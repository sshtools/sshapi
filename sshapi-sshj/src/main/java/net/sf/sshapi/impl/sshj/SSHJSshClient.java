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
package net.sf.sshapi.impl.sshj;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import net.schmizz.sshj.Config;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder.Forward;
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.keyprovider.PKCS5KeyFile.DecryptException;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.forwarding.AbstractPortForward;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.sftp.SftpClient;

class SSHJSshClient extends AbstractClient {

	private SSHClient ssh;

	public SSHJSshClient(Config underlyingConfiguration, SshConfiguration configuration) {
		super(configuration);
		ssh = new SSHClient(underlyingConfiguration);
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		try {
			if (!doAuthentication(authenticators)) {
				return false;
			}
			checkForBanner();
			return true;
		} catch (SshException sshe) {
			throw sshe;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	@Override
	public boolean isConnected() {
		return ssh.isConnected();
	}

	@Override
	public boolean isAuthenticated() {
		return ssh.isAuthenticated();
	}

	@Override
	public String getRemoteIdentification() {
		return ssh.getTransport().getServerVersion();
	}

	@Override
	public int getRemoteProtocolVersion() {
		return SshConfiguration.SSH2_ONLY;
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

	@Override
	protected SshSCPClient doCreateSCP() throws SshException {
		return new SSHJSCPClient(this);
	}

	@Override
	protected SftpClient doCreateSftp() throws SshException {
		return new SSHJSftpClient(this);
	}

	@Override
	protected SshPortForward doCreateRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		RemotePortForwarder remotePortForward = ssh.getRemotePortForwarder();

		return new AbstractPortForward(getProvider()) {

			Forward fwd;

			@Override
			public int getBoundPort() {
				return fwd == null ? 0 : fwd.getPort();
			}

			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					remotePortForward.cancel(fwd);
				} catch (ConnectionException | TransportException e) {
					throw new SshException(SshException.IO_ERROR, "Failed to close remote forward.", e);
				}
			}

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					fwd = remotePortForward.bind(new Forward(remoteBindAddress, remoteBindPort),
							new SocketForwardingConnectListener(new InetSocketAddress(targetAddress, targetPort)));
				} catch (ConnectionException | TransportException e) {
					throw new SshException(SshException.IO_ERROR, "Failed to open remote forward.", e);
				}
			}
		};
	}

	@Override
	protected SshPortForward doCreateLocalForward(String localBindAddress, int localBindPort, String targetAddress,
			int targetPort) throws SshException {

		final String fLocalAddress = localBindAddress == null ? "0.0.0.0" : localBindAddress;
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(localBindPort,
					Integer.parseInt(getConfiguration().getProperties()
							.getProperty(SSHJSshProvider.CFG_LOCAL_FORWARD_BACKLOG, "59")),
					InetAddress.getByName(fLocalAddress));
		} catch (NumberFormatException | IOException e1) {
			throw new SshException(SshException.IO_ERROR, "Failed to create local forward.", e1);
		}
		Parameters parameters = new Parameters(fLocalAddress, localBindPort, targetAddress, targetPort);
		LocalPortForwarder localPortForward = ssh.newLocalPortForwarder(parameters, serverSocket);

		return new AbstractPortForward(getProvider()) {

			@Override
			public int getBoundPort() {
				return serverSocket.getLocalPort();
			}

			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					localPortForward.close();
				} catch (IOException e) {
					throw new net.sf.sshapi.SshException("Failed to stop local forward.", e);
				}
			}

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				new Thread("SSHAPI-SSHJ-LocalForward-" + fLocalAddress + ":" + localBindPort + "->" + targetAddress
						+ ":" + targetPort) {
					public void run() {
						try {
							localPortForward.listen();
						} catch (IOException e) {
							throw new IllegalStateException("Failed to start local forward.", e);
						}
					}
				}.start();
			}
		};
	}

	@Override
	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws net.sf.sshapi.SshException {
		Session session;
		try {
			session = ssh.startSession();
		} catch (ConnectionException | TransportException e) {
			throw new SshException(SshException.FAILED_TO_OPEN_SHELL, e.getMessage(), e);
		}
		return new SSHJSshShell(this, session, termType, cols, rows, pixWidth, pixHeight, terminalModes);
	}

	@Override
	protected SshCommand doCreateCommand(String command, String termType, int cols, int rows, int pixWidth,
			int pixHeight, byte[] terminalModes) throws SshException {

		try {
			final Session sess = ssh.startSession();
			if (termType != null) {
				sess.allocatePTY(termType, cols, rows, 0, 0, SSHJSshShell.toModeMap(terminalModes));
			}
			return new SSHJStreamChannel(getProvider(), getConfiguration(), sess) {
				private Command commandHandle;

				@Override
				public boolean isOpen() {
					return super.isOpen() && commandHandle != null;
				}

				@Override
				public void onChannelOpen() throws SshException {
					try {
						this.commandHandle = sess.exec(command);
					} catch (IOException e) {
						throw new SshException(SshException.IO_ERROR, e);
					}
				}

				@Override
				public InputStream getExtendedInputStream() throws IOException {
					return commandHandle.getErrorStream();
				}

				@Override
				public int exitCode() throws IOException {
					Integer exitStatus = commandHandle.getExitStatus();
					return exitStatus == null ? SshCommand.EXIT_CODE_NOT_RECEIVED : exitStatus;
				}
			};
		} catch (IOException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		try {
			SshHostKeyValidator val = getConfiguration().getHostKeyValidator();
			if (val != null)
				ssh.addHostKeyVerifier(new HostKeyVerifier() {

					@Override
					public boolean verify(String hostname, int port, PublicKey key) {
						try {
							return val.verifyHost(new SshHostKey() {

								@Override
								public String getType() {
									return KeyType.fromKey(key).toString();
								}

								@Override
								public byte[] getKey() {
									return key.getEncoded();
								}

								@Override
								public String getHost() {
									return hostname;
								}

								@Override
								public String getFingerprint() {
									return SecurityUtils.getFingerprint(key);
								}
							}) == SshHostKeyValidator.STATUS_HOST_KEY_VALID;
						} catch (SshException sshe) {
							throw new IllegalStateException("Failed to verify host.", sshe);
						}
					}

				});
			ssh.connect(hostname, port);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", e);
		}
	}

	@Override
	protected void onClose() throws SshException {
		try {
			ssh.close();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to close.", e);
		}
	}

	private void checkForBanner() {
		try {
			String banner = ssh.isConnected() ? ssh.getUserAuth().getBanner() : null;
			if (banner != null && !banner.equals("") && getConfiguration().getBannerHandler() != null) {
				getConfiguration().getBannerHandler().banner(banner);
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().error("Failed to access banner", e);
		}
	}

	private boolean doAuthentication(SshAuthenticator[] authenticators) throws IOException, SshException {
		try {
			ssh.auth(getUsername(), toNativeAuthMethods(authenticators));
			return true;
		} catch (UserAuthException uae) {
			if (uae.getCause() instanceof UserAuthException) {
				uae = (UserAuthException) uae.getCause();
				if (!(uae.getCause() instanceof DecryptException) && uae.getMessage() != null
						&& uae.getMessage().indexOf("Problem getting public key") != -1) {
					throw new SshException(SshException.PRIVATE_KEY_FORMAT_NOT_SUPPORTED, uae.getMessage(), uae);
				}
			}
			return false;
		}
	}

	private Iterable<AuthMethod> toNativeAuthMethods(SshAuthenticator[] authenticators) {
		List<AuthMethod> methods = new ArrayList<>();
		for (SshAuthenticator auth : authenticators) {
			if (auth instanceof SshPasswordAuthenticator) {
				SshPasswordAuthenticator pw = (SshPasswordAuthenticator) auth;
				methods.add(new AuthPassword(new PasswordFinder() {
					@Override
					public boolean shouldRetry(Resource<?> resource) {
						return false;
					}

					@Override
					public char[] reqPassword(Resource<?> resource) {
						return pw.promptForPassword(SSHJSshClient.this, "Password");
					}
				}));
			} else if (auth instanceof SshPublicKeyAuthenticator) {
				SshPublicKeyAuthenticator pka = (SshPublicKeyAuthenticator) auth;
				methods.add(new AuthPublickey(new KeyProvider() {

					KeyProvider keyProvider;

					void getKey() throws IOException {
						if (keyProvider == null) {
							keyProvider = ssh.loadKeys(new String(pka.getPrivateKey()), null, new PasswordFinder() {
								@Override
								public boolean shouldRetry(Resource<?> resource) {
									return false;
								}

								@Override
								public char[] reqPassword(Resource<?> resource) {
									char[] ps = pka.promptForPassphrase(SSHJSshClient.this, "Passphrase");
									return ps == null ? new char[0] : ps;
								}
							});
						}
					}

					@Override
					public KeyType getType() throws IOException {
						getKey();
						return keyProvider.getType();
					}

					@Override
					public PublicKey getPublic() throws IOException {
						getKey();
						return keyProvider.getPublic();
					}

					@Override
					public PrivateKey getPrivate() throws IOException {
						getKey();
						return keyProvider.getPrivate();
					}
				}));
			} 
			else if (auth instanceof SshKeyboardInteractiveAuthenticator) {
				SshKeyboardInteractiveAuthenticator pka = (SshKeyboardInteractiveAuthenticator) auth;
				methods.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
					
					private String instruction;
					private String name;

					@Override
					public boolean shouldRetry() {
						return false;
					}
					
					@Override
					public void init(Resource resource, String name, String instruction) {
						this.name = name;
						this.instruction = instruction;						
					}
					
					@Override
					public List<String> getSubmethods() {
						return null;
					}
					
					@Override
					public char[] getResponse(String prompt, boolean echo) {
						String[] rep = pka.challenge(name, instruction, new String[] {prompt}, new boolean[] { echo });
						return rep == null || rep.length == 0 ? null : rep[0].toCharArray();
					}
				}));
			}
			else
				throw new UnsupportedOperationException();
		}

		return methods;
	}

	SSHClient getSsh() {
		return ssh;
	}
}
