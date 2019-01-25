package net.sf.sshapi.impl.openssh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;

public class OpenSshClient extends AbstractClient {

	private boolean authenticated;
	private boolean connected;
	private Process process;
	private File pipeIn;
	private File pipeOut;
	private File askPass;
	private Thread pipeInThread;
	private Semaphore connectSemaphore = new Semaphore(1);

	public OpenSshClient(SshConfiguration configuration) {
		super(configuration);
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		authenticated = true;
		return true;
	}

	@Override
	public void disconnect() throws SshException {
		if (pipeInThread != null)
			pipeInThread.interrupt();
		askPass.delete();
		pipeIn.delete();
		pipeOut.delete();
		connected = false;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
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
	public String getUsername() {
		return null;
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		boolean found = false;
		for (SshAuthenticator a : authenticators) {
			if (!(a instanceof SshPasswordAuthenticator))
				throw new UnsupportedOperationException(
						String.format("Authenticator type %s is not supported by this provider.", a.getClass()));
			else
				found = true;
		}
		if (!found)
			throw new UnsupportedOperationException(
					String.format("Must provide an authenticator of type %s.", SshPasswordAuthenticator.class));

		pipeIn = createPipe();
		pipeOut = createPipe();

		ProcessBuilder pb = new ProcessBuilder("ssh", "-v", "-v", "-v", String.format("%s@%s", username, hostname), "-p",
				String.valueOf(port));
		pb.environment().put("SSH_AUTH_PIPE_IN_FILE", pipeIn.getAbsolutePath());
		pb.environment().put("SSH_AUTH_PIPE_OUT_FILE", pipeOut.getAbsolutePath());

		try {
			askPass = File.createTempFile("sshapi-openssh", "askpass.sh");
			askPass.deleteOnExit();
			Files.setPosixFilePermissions(askPass.toPath(), new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ,
					PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)));
			try (InputStream in = getClass().getResourceAsStream("/askpass.sh")) {
				try (FileOutputStream out = new FileOutputStream(askPass)) {
					int r;
					byte[] buf = new byte[65536];
					while ((r = in.read(buf)) != -1)
						out.write(buf, 0, r);
				}
			}

			pb.environment().put("SSH_ASKPASS", askPass.getAbsolutePath());

			/*
			 * Now start a thread to read the pipe file waiting for authentication requests
			 */
			pipeInThread = new Thread("SSHAPI-OpenSSH-PipeIn") {
				public void run() {
					try {
						try (BufferedReader r = new BufferedReader(
								new InputStreamReader(new FileInputStream(pipeIn)))) {
							String line = null;
							while ((line = r.readLine()) != null) {
								if (line.equals("ready-for-password")) {
									System.out.println("PROMPTING");
									SshPasswordAuthenticator pw = (SshPasswordAuthenticator) authenticators[0];
									char[] pwd = pw.promptForPassword(OpenSshClient.this, "Password");
									try (PrintWriter pipeOutWriter = new PrintWriter(new FileWriter(pipeOut), true)) {
										pipeOutWriter.println(pwd == null ? "" : new String(pwd));
									}
									System.out.println("SENT" + (pwd == null ? "" : new String(pwd)));
								}
							}
						}
					} catch (Exception ie) {
						// Done
					}
				};
			};
			pipeInThread.start();

			pb.redirectErrorStream(true);
			process = pb.start();

			try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line = null;
				while ((line = r.readLine()) != null) {
					System.out.println(line);
				}
			}

		} catch (IOException e) {
			throw new SshException(SshException.GENERAL, "Failed to start OpenSSH process.", e);
		}
		connected = true;
	}

	File createPipe() throws SshException {
		try {
			File f = File.createTempFile("sshapi-openssh", "pipe");
			f.delete();
			ProcessBuilder pb = new ProcessBuilder("mknod", f.getAbsolutePath(), "p");
			process = pb.start();
			f.deleteOnExit();
			return f;
		} catch (IOException e) {
			throw new SshException(SshException.GENERAL, "Failed to create OpenSSH authentication pipes.", e);
		}
	}

}
