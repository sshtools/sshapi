package net.sf.sshapi.impl.openssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.IOUtils;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAgentAuthenticator;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.sftp.SftpClient;

public class OpenSshClient extends AbstractClient {
	final static int ARGS_SFTP = 0;
	final static int ARGS_SSH = 1;

	public static ProcessBuilder configureCommand(File dir, ProcessBuilder pb) {
		pb.directory(dir);
		pb.redirectErrorStream(true);
		return pb;
	}

	public static Process run(ProcessBuilder pb) throws IOException, InterruptedException {
		Process process = pb.start();
		IOUtils.copy(process.getInputStream(), System.out);
		process.waitFor();
		return process;
	}

	public static Process runAndCheckReturn(ProcessBuilder pb) throws IOException, InterruptedException {
		Process process = pb.start();
		IOUtils.copy(process.getInputStream(), System.out);
		if (process.waitFor() != 0) {
			throw new IOException("Command returned " + process.exitValue() + ".");
		}
		return process;
	}

	public static Process runAndCheckReturnWithFileInput(final File input, ProcessBuilder pb)
			throws IOException, InterruptedException {
		final Process process = pb.start();
		final FileInputStream fin = new FileInputStream(input);
		try {
			Thread t = new Thread("Stdin") {
				public void run() {
					try {
						IOUtils.copy(process.getInputStream(), System.out);
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			};
			t.start();
			IOUtils.copy(fin, process.getOutputStream());
			process.getOutputStream().close();
			process.getInputStream().close();
			if (process.waitFor() != 0) {
				throw new IOException("Command returned " + process.exitValue() + ".");
			}
		} finally {
			fin.close();
		}
		return process;
	}

	protected static void debugArgs(List<String> args) {
		SshConfiguration.getLogger().info("Arguments: {0}", String.join(" ", args));
		System.out.println(String.format("Arguments: %s", String.join(" ", args)));
	}

	private boolean authenticated;
	private Map<String, SshAuthenticator> authenticators = new HashMap<>();
	private String cachedPassphrase;
	private String cachedPassword;
	private boolean connected;
	private String hostname;
	private int port;
	private SshPublicKeyAuthenticator pubkey;
	private String username;

	public OpenSshClient(SshConfiguration configuration) {
		super(configuration);
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		boolean res = processAuthenticators(authenticators);
		if (res) {
			/* Start an SFTP client client to test the authenticator. */
			// try (SftpClient sftp = sftp()) {
			try (SshCommand cmd = command("sleep 0.1")) {
			} catch (SshException sshe) {
				cachedPassphrase = null;
				cachedPassword = null;
				if (sshe.getCode() == SshException.AUTHENTICATION_FAILED) {
					return false;
				}
			} catch (IOException ioe) {
				cachedPassphrase = null;
				cachedPassword = null;
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		}
		return res;
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

	@Override
	public String getRemoteIdentification() {
		return null;
	}

	@Override
	public int getRemoteProtocolVersion() {
		return 0;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	protected void add(List<String> args, String opt, int fmt) {
		switch (fmt) {
		case ARGS_SFTP:
			args.add("-o" + opt);
			break;
		default:
			args.add("-o");
			args.add(opt);
			break;
		}
	}

	protected void agentAuthentication(ProcessBuilder pb, boolean enabled) {
		if (enabled) {
			// SshAgent agent = agent.getAgent(getConfiguration());
		} else
			pb.environment().put("SSH_AUTH_SOCK", "");
	}

	protected List<String> challengeResponseAuthentication(List<String> args, boolean enabled, int fmt) {
		add(args, "ChallengeResponseAuthentication=" + yesNo(enabled), fmt);
		return args;
	}

	protected ProcessBuilder configureSshCommand(ProcessBuilder pb) {
		configureCommand(new File(System.getProperty("user.dir")), pb);
		return pb;
	}

	protected List<String> createSCPArgs() {
		List<String> l = new ArrayList<String>();
		l.add("unbuffer");
		l.add("-p");
		l.add(getConfiguration().getProperties().getProperty(OpenSshProvider.CFG_OPENSSH_SCP_COMMAND, "scp"));
		l.add("-P");
		l.add(String.valueOf(port));
		return l;
	}

	protected List<String> createSftpArgs() {
		List<String> l = new ArrayList<String>();
		l.add("unbuffer");
		l.add("-p");
		l.add(getConfiguration().getProperties().getProperty(OpenSshProvider.CFG_OPENSSH_SFTP_COMMAND, "sftp"));
		l.add("-oBatchMode=no");
		l.add("-P");
		l.add(String.valueOf(port));
		return l;
	}

	protected List<String> createSshArgs() {
		List<String> l = new ArrayList<String>();
		l.add("unbuffer");
		l.add("-p");
		l.add(getConfiguration().getProperties().getProperty(OpenSshProvider.CFG_OPENSSH_SSH_COMMAND, "ssh"));
		l.add("-p");
		l.add(String.valueOf(port));
		return l;
	}

	protected List<String> createLocalFwdArgs(String localBindAddress, int localBindPort, String targetAddress, int targetPort) {
		List<String> l = new ArrayList<String>();
		l.add("unbuffer");
		l.add("-p");
		l.add(getConfiguration().getProperties().getProperty(OpenSshProvider.CFG_OPENSSH_SSH_COMMAND, "ssh"));
		l.add("-p");
		l.add(String.valueOf(port));
		l.add("-L");
		String spec = localBindAddress == null || localBindAddress.equals("0.0.0.0") ? "*" : localBindAddress;
		spec += ":";
		spec += localBindPort;
		spec += ":";
		spec += targetAddress;
		spec += ":";
		spec += targetPort;
		l.add(spec);
		l.add("-N");
		return l;
	}

	protected List<String> createCmdArgs() {
		List<String> l = new ArrayList<String>();
		l.add(getConfiguration().getProperties().getProperty(OpenSshProvider.CFG_OPENSSH_SSH_COMMAND, "ssh"));
		l.add("-p");
		l.add(String.valueOf(port));
		return l;
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators) throws SshException {
		this.port = port;
		this.hostname = hostname;
		this.username = username;
		/* See if there is a socket to connect to */
		try {
			Socket s = new Socket(hostname, port);
			s.setReuseAddress(true);
			s.setSoLinger(false, 0);
			s.close();
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to connect to %s on port %d", hostname, port));
		}
		if (authenticators.length > 0 && !processAuthenticators(authenticators))
			throw new SshException(SshException.AUTHENTICATION_FAILED);
		connected = true;
	}

	@Override
	protected SshPortForward doCreateLocalForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		ProcessBuilder pb = createSshCommand(createLocalFwdArgs(localBindAddress, localBindPort, targetAddress, targetPort), true,
				ARGS_SSH);
		return new OpenSshLocalForward(this, configureSshCommand(pb));
	}

	@Override
	protected SshCommand doCreateCommand(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		ProcessBuilder pb = createSshCommand(createCmdArgs(), true, ARGS_SSH);
		return new OpenSshCommand(this, configureSshCommand(pb), termType, command);
	}

	@Override
	protected SshSCPClient doCreateSCP() throws SshException {
		ProcessBuilder pb = createSshCommand(createSCPArgs(), false, ARGS_SFTP);
		return new OpenSshSCPClient(configureSshCommand(pb), this);
	}

	@Override
	protected SftpClient doCreateSftp() throws SshException {
		ProcessBuilder pb = createSshCommand(createSftpArgs(), true, ARGS_SFTP);
		return new OpenSshSftpClient(this, configureSshCommand(pb));
	}

	@Override
	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		ProcessBuilder pb = createSshCommand(createSshArgs(), true, ARGS_SSH);
		return new OpenSshShell(this, configureSshCommand(pb), termType);
	}

	protected List<String> hostKeyVerification(List<String> args, boolean dumb, int fmt) {
		if (dumb) {
			add(args, "StrictHostKeyChecking=no", fmt);
			add(args, "UserKnownHostsFile=/dev/null", fmt);
		}
		return args;
	}

	@Override
	protected void onClose() throws SshException {
		for (SshLifecycleComponent<?, ?> c : activeComponents)
			c.closeQuietly();
		// if (pipeInThread != null)
		// pipeInThread.interrupt();
		// askPass.delete();
		// pipeIn.delete();
		// pipeOut.delete();
		connected = false;
	}

	protected List<String> passwordAuthentication(List<String> args, boolean enabled, int fmt) {
		add(args, "PasswordAuthentication=" + yesNo(enabled), fmt);
		return args;
	}

	protected List<String> publicKeyAuthentication(List<String> args, boolean enabled, int fmt) {
		add(args, "PubkeyAuthentication=" + yesNo(enabled), fmt);
		if (enabled) {
			add(args, "IdentitiesOnly=yes", fmt);
			args.add("-i");
			args.add(pubkey.getPrivateKeyFile().getAbsolutePath());
		}
		return args;
	}

	protected List<String> sshTail(String username, List<String> l, String command) {
		l.add((username == null ? getUsername() : username) + "@" + hostname);
		if (command != null) {
			l.add(command);
		}
		return l;
	}

	protected String yesNo(boolean enabled) {
		return enabled ? "yes" : "no";
	}

	ProcessBuilder setupAuthentication(ProcessBuilder process) throws IOException {
		if (authenticators.containsKey("password")) {
			if (cachedPassword == null)
				throw new IOException("Password authentication cancelled.");
			if (process.command().get(0).equals("unbuffer")) {
				process.command().add(2, "sshpass");
				process.command().add(3, "-p");
				process.command().add(4, cachedPassword);
			} else {
				process.command().add(0, "sshpass");
				process.command().add(1, "-p");
				process.command().add(2, cachedPassword);
			}
		} else if (authenticators.containsKey("public-key")) {
			if (cachedPassphrase != null && cachedPassphrase.equals("")) {
				if (process.command().get(0).equals("unbuffer")) {
					process.command().add(2, "sshpass");
					process.command().add(3, "-v");
					process.command().add(3, "-p");
					process.command().add(4, cachedPassphrase == null ? "" : cachedPassphrase);
				} else {
					process.command().add(0, "sshpass");
					process.command().add(3, "-v");
					process.command().add(1, "-p");
					process.command().add(2, cachedPassphrase == null ? "" : cachedPassphrase);
				}
			} else
				process.environment().put("DISPLAY", "");
		}
		debugArgs(process.command());
		return process;
	}

	private ProcessBuilder createSshCommand(List<String> args, boolean tail, int fmt) throws SshException {
		args.add("-q");
		hostKeyVerification(args, true, fmt);
		passwordAuthentication(args, authenticators.containsKey("password"), fmt);
		publicKeyAuthentication(args, authenticators.containsKey("public-key"), fmt);
		challengeResponseAuthentication(args, authenticators.containsKey("keyboard-interactive"), fmt);
		if (tail)
			sshTail(username, args, null);
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		agentAuthentication(processBuilder, authenticators.containsKey("agent"));
		return processBuilder;
	}

	private boolean processAuthenticators(SshAuthenticator[] authenticators) throws SshException {
		boolean ok = false;
		for (SshAuthenticator auth : authenticators) {
			if (auth instanceof SshPasswordAuthenticator && !this.authenticators.containsKey("password")) {
				if (cachedPassword == null) {
					this.authenticators.put("password", auth);
					char[] pw = ((SshPasswordAuthenticator) auth).promptForPassword(OpenSshClient.this, "Password:");
					if (pw == null) {
						cachedPassword = null;
						ok = false;
					} else {
						cachedPassword = new String(pw);
						ok = true;
					}
				} else {
					ok = true;
				}
			} else if (auth instanceof SshPublicKeyAuthenticator && !this.authenticators.containsKey("public-key")) {
				SshPublicKeyAuthenticator pk = ((SshPublicKeyAuthenticator) auth);
				if (cachedPassphrase != null) {
					this.authenticators.put("public-key", auth);
					pubkey = pk;
					ok = true;
				}
				cachedPassphrase = null;
				/*
				 * Use ssh-keygen to determine if we need a password for this
				 * key.
				 * 
				 * First determine if there is a passphrase at all
				 */
				ProcessBuilder pb = new ProcessBuilder(getConfiguration().getProperties()
						.getProperty(OpenSshProvider.CFG_OPENSSH_SSH_KEYGEN_COMMAND, "ssh-keygen"), "-y", "-f",
						pk.getPrivateKeyFile().getAbsolutePath());
				AtomicBoolean failed = new AtomicBoolean();
				try {
					pb.redirectErrorStream(true);
					pb.environment().put("DISPLAY", "");
					Process prc = pb.start();
					String result = IOUtils.toString(prc.getInputStream(), "UTF-8");
					prc.waitFor(10, TimeUnit.SECONDS);
					if (result.startsWith("Load key")) {
						throw new SshException(SshException.PRIVATE_KEY_FORMAT_NOT_SUPPORTED, result);
					} else if (result.startsWith("ssh-")) {
						/* No passphrase */
						this.authenticators.put("public-key", auth);
						pubkey = pk;
						ok = true;
					} else {
						ok = false;
					}
				} catch (IOException ioe) {
					throw new SshException(SshException.IO_ERROR, "Failed to run ssh-keygen.", ioe);
				} catch (InterruptedException e) {
					throw new SshException(SshException.IO_ERROR, "Failed to run ssh-keygen.", e);
				}
				if (!ok) {
					/* Have passphrase, check it using sshpass and ssh-keygen */
					char[] ps = pk.promptForPassphrase(this, "Passphrase: ");
					if (ps != null) {
						pb = new ProcessBuilder(
								"unbuffer", "sshpass", "-p", new String(ps), "-P", "phrase", getConfiguration().getProperties()
										.getProperty(OpenSshProvider.CFG_OPENSSH_SSH_KEYGEN_COMMAND, "ssh-keygen"),
								"-y", "-f", pk.getPrivateKeyFile().getAbsolutePath());
						try {
							pb.redirectErrorStream(true);
							Process prc = pb.start();
							String result = IOUtils.toString(prc.getInputStream(), "UTF-8");
							if (result.startsWith("ssh-")) {
								cachedPassphrase = new String(ps);
								this.authenticators.put("public-key", auth);
								pubkey = pk;
								ok = true;
							} else {
								ok = false;
							}
						} catch (IOException ioe) {
							throw new SshException(SshException.IO_ERROR, "Failed to run ssh-keygen.", ioe);
						}
					} else
						ok = false;
				}
			} else if (auth instanceof SshKeyboardInteractiveAuthenticator
					&& !this.authenticators.containsKey("keyboard-interactive")) {
				this.authenticators.put("keyboard-interactive", auth);
			} else if (auth instanceof SshAgentAuthenticator && !this.authenticators.containsKey("agent")) {
				this.authenticators.put("agent", auth);
			}
			if (ok)
				break;
		}
		authenticated = ok;
		return ok;
	}
}
