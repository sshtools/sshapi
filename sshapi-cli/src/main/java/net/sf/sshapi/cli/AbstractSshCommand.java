/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.sf.sshapi.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.BatchHostKeyValidator;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleKeyboardInteractiveAuthenticator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.DefaultAgentAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import picocli.CommandLine.Option;

/**
 * Abstract command.
 */
public abstract class AbstractSshCommand implements Logger {

	protected SshConfiguration configuration;
	protected Terminal terminal;
	protected LineReader reader;
	protected SshProvider provider;
	protected Level level;
	protected boolean traces;

	@Option(names = { "--provider" }, description = "Provider class name.")
	private String providerClass;

	@Option(names = { "-C", "--compress" }, description = "Enabled compression.")
	private boolean compress;

	@Option(names = { "-c", "--cipher" }, description = "Cipher.")
	private String cipher;

	@Option(names = { "-v", "--verbosity" }, description = "Log verbosity.")
	private int verbosity;

	@Option(names = { "-1", "--ssh-1" }, description = "Force use of SSH1.")
	private boolean v1;

	@Option(names = { "-2", "--ssh-2" }, description = "Force use of SSH2.")
	private boolean v2;

	@Option(names = { "-q", "--quiet" }, description = "No output.")
	private boolean quiet;

	@Option(names = { "--ssh-auth-sock" }, description = "Identifies the path of a UNIX-domain socket used to communicate with the agent.")
	private File sshAuthSock;

	@Option(names = { "-i", "--identity-file" }, description = "elects the file from which the identity (private key) for public key authentication is read.")
	private File identityFile;
	
	private SshAgent agent;

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public AbstractSshCommand() throws SshException {
		SshConfiguration.setLogger(this);
		configuration = new SshConfiguration();
		try {
			terminal = TerminalBuilder.builder().system(true).build();
			reader = LineReaderBuilder.builder().terminal(terminal).build();
			// terminal.beforeReadLine(reader, "", (char)0);
		} catch (Exception e) {
			if(verbosity > 1)
				e.printStackTrace();
			terminal = null;
		}

	}

	/**
	 * Start the command.
	 * 
	 * @throws SshException
	 * @throws IOException
	 */
	public final void start() throws SshException, IOException {
		switch (verbosity) {
		case 0:
			level = quiet ? Level.ERROR : Level.WARN;
			break;
		case 1:
			level = Level.INFO;
			break;
		case 2:
			level = Level.DEBUG;
			break;
		default:
			level = Level.DEBUG;
			traces = true;
			break;
		}

		provider = null;
		if (providerClass != null) {
			try {
				provider = (SshProvider) Class.forName(providerClass).getConstructor().newInstance();
			} catch (Exception e) {
				warn("SSH provider {0} not accessible: Falling back to first available provider.", providerClass);
			}
		}
		if (provider == null) {
			provider = DefaultProviderFactory.getInstance().getProvider(configuration);
		}

		if (compress) {
			if (provider.getSupportedCompression().size() > 0) {
				configuration.setPreferredClientToServerCompression((String) provider.getSupportedCompression().get(0));
				configuration.setPreferredServerToClientCompression((String) provider.getSupportedCompression().get(0));
			} else {
				warn("SSH provider {0} does not support compression, disabling.", providerClass);
			}
		}

		if (cipher != null) {
			List<String> ciphers = provider.getSupportedCiphers(configuration.getProtocolVersion());
			if (ciphers.contains(cipher)) {
				throw new SshException(SshException.UNSUPPORTED_FEATURE, "The cipher " + cipher + " is not supported.");
			}
			configuration.setPreferredClientToServerCipher(cipher);
			configuration.setPreferredServerToClientCipher(cipher);
		}

		SshHostKeyManager keyManager = provider.createHostKeyManager(configuration);
		SshHostKeyValidator validator = null;
		if (isBatchMode()) {
			if (keyManager == null) {
				throw new SshException("This provider does not support key management, so batch mode may not be used.");
			}
			validator = new BatchHostKeyValidator(keyManager);
		} else {
			validator = new ConsoleHostKeyValidator(keyManager);
		}
		configuration.setHostKeyValidator(validator);

		// Protocol version
		if (v1) {
			configuration.setProtocolVersion(SshConfiguration.SSH1_ONLY);
			if (v2) {
				throw new SshException(
						"Conflicting options -1 and -2. You may only specify one or the other or neither.");
			}
		} else if (v2) {
			configuration.setProtocolVersion(SshConfiguration.SSH1_ONLY);
		}

		onStart();
	}

	protected boolean isQuiet() {
		return quiet;
	}

	protected abstract boolean isBatchMode();

	protected abstract void onStart() throws SshException, IOException;

	protected SshClient connect(String connectionDetails) throws SshException, IOException {
			

		File auth = this.sshAuthSock;
		if (provider.getCapabilities().contains(Capability.AGENT) && agent == null) {
			if(auth == null) {
				String authStr = System.getenv("SSH_AUTH_SOCK");
				if(authStr != null && authStr.length() > 0)
					auth = new File(authStr);
			}
			auth = authSockWorkaround(auth);
			if(auth != null) {
				info("Using {0} for agent socket", auth);
				try {
					int type = SshAgent.AUTO_AGENT_SOCKET_TYPE;
					info("Starting agent");
					agent = provider.connectToLocalAgent(getClass().getSimpleName(), auth.getAbsolutePath(), type, SshAgent.AUTO_PROTOCOL);
					info("Started agent");
				} catch (SshException e) {
					if (auth != null && auth.length() > 0) {
						if(!quiet)
							System.err.println(
									"Failed to to agent, and SSH_AUTH_SOCK was set, so it is expected to be working. Will fall back to other authentication methods.");
					} else
						info("Could not connect to agent, but no SSH_AUTH_SOCK was set, so it is probably not running.");
				}
			}
		}
		
		// Connect
		SshClient client = provider.createClient(configuration);
		client.connect(extractUsername(connectionDetails), extractHostname(connectionDetails), getPort());
		List<SshAuthenticator> authenticators = new ArrayList<>();
		if(auth != null && agent != null) {
			client.addChannelHandler(agent);
			authenticators.add(new DefaultAgentAuthenticator(agent));
		}
		SshPasswordAuthenticator pwAuth = null;

		// Create the non-batch authenticators
		if (!isBatchMode()) {
			if (terminal == null) {
				pwAuth = new ConsolePasswordAuthenticator();
			} else {
				pwAuth = new JLinePasswordAuthenticator(reader);
			}
		}

		// Key authentication
		File identityFile = this.identityFile;
		if(identityFile == null) {
			for(String pk : provider.getSupportedPublicKey()) {
				String name = null;
				if(pk.equals(SshConfiguration.PUBLIC_KEY_SSHDSA)) {
					name = "id_dsa";
				}
				else if(pk.equals(SshConfiguration.PUBLIC_KEY_SSHRSA)) {
					name = "id_rsa";
				}
				else if(pk.equals(SshConfiguration.PUBLIC_KEY_ED25519)) {
					name = "id_ed25519";
				}
				else if(pk.equals(SshConfiguration.PUBLIC_KEY_ECDSA_256) || pk.equals(SshConfiguration.PUBLIC_KEY_ECDSA_521) || pk.equals(SshConfiguration.PUBLIC_KEY_ECDSA_384)) {
					name = "id_ecdsa";
				}
				if(name != null) {
					File pkFile = new File(new File(System.getProperty("user.home"), ".ssh"), name);
					if(pkFile.exists()) {
						identityFile = pkFile;
					}
				}
			}
		}
		if (identityFile != null) {
			if(identityFile.exists()) {
				info("Using {0} for key authentication", identityFile);
				authenticators.add(new DefaultPublicKeyAuthenticator(pwAuth, identityFile));
			}
			else
				throw new FileNotFoundException(String.format("No such file %s", identityFile));
		}

		// Add the non-batch authenticators
		if (!isBatchMode()) {
			authenticators.add(pwAuth);
			authenticators.add(new ConsoleKeyboardInteractiveAuthenticator());
		}

		// Now authenticate

		info("Using authenticators: {0}", authenticators);
		if (client.authenticate(authenticators)) {
			return client;
		}

		// Failed
		throw new SshException("Permission denied.");
	}

	protected abstract int getPort();

	String extractUsername(String connectionDetails) {
		int idx = connectionDetails.indexOf('@');
		if (idx == -1) {
			throw new IllegalArgumentException("User name for remote path not provided.");
		}
		return connectionDetails.substring(0, idx);
	}

	String extractHostname(String connectionDetails) {
		int idx = connectionDetails.indexOf('@');
		return idx == -1 ? connectionDetails : connectionDetails.substring(idx + 1);
	}

	String getConnectionDetails(String path) {
		int idx = path.indexOf(":");
		if (idx == -1) {
			return null;
		}
		return path.substring(0, idx);
	}

	String getPath(String path) {
		int idx = path.indexOf(":");
		if (idx == -1) {
			return null;
		}
		return path.substring(idx + 1);
	}

	boolean isRemotePath(String path) {
		int idx = path.indexOf("@");
		int idx2 = path.indexOf(":");
		return idx > -1 && idx2 > -1 && idx2 > idx;
	}

	@Override
	public void log(Level level, String message, Object... args) {
		if (isLevelEnabled(level)) {
			System.err.println(level.name() + ": " + MessageFormat.format(message, args));
		}
	}

	@Override
	public void log(Level level, String message, Throwable exception, Object... args) {
		if (isLevelEnabled(level)) {
			log(level, message, args);
			if (traces && exception != null) {
				exception.printStackTrace();
			}
		}
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return this.level.compareTo(level) <= 0;
	}

	private File authSockWorkaround(File auth) {
		if (auth == null) {
			File f = new File(System.getProperty("user.home") + File.separator + ".desktop-ssh-agent" + File.separator + "agent.sock");
			if (f.exists()) {
				try {
					/*
					 * Should point to a link, check that exists before using
					 * the link path
					 */
					File canon = f.getCanonicalFile();
					if (canon.exists())
						auth = f;
				} catch (IOException ioe) {
				}
			}
		}
		return auth;
	}
}
