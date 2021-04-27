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
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;
import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.sftp.SftpClient;

class SSHJSshClient extends AbstractClient {

	private SSHClient ssh;

	public SSHJSshClient(SshConfiguration configuration) {
		super(configuration);
		ssh = new SSHClient();
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
			} else
				throw new UnsupportedOperationException();
		}

//		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
//		while (true) {
//			String[] remainingMethods = ssh.getUserAuth().getAllowedMethods();
//			if (remainingMethods == null || remainingMethods.length == 0) {
//				throw new SshException(SshException.AUTHENTICATION_FAILED, "No remaining authentication methods");
//			}
//			for (int i = 0; i < remainingMethods.length; i++) {
//				SshAuthenticator authenticator = authenticatorMap.get(remainingMethods[i]);
//				if (authenticator != null) {
//					// Password
//					if (authenticator instanceof SshPasswordAuthenticator) {
//						char[] pw = ((SshPasswordAuthenticator) authenticator).promptForPassword(this, "Password");
//						if (pw == null) {
//							throw new SshException("Authentication cancelled.");
//						}
//						try {
//							ssh.authPassword(getUsername(), pw);
//							return true;
//						}
//						catch(UserAuthException uae) {
//						}
//						// Return to main loop so getRemainingMethods is called
//						// again
//						continue;
//					}
//					// Public key
//					if (authenticator instanceof SshPublicKeyAuthenticator) {
//						SshPublicKeyAuthenticator pk = (SshPublicKeyAuthenticator) authenticator;
//						try {
//							ssh.authPublickey(getUsername(), new KeyProvider() {
//								@Override
//								public KeyType getType() throws IOException {
//									// TODO Auto-generated method stub
//									return null;
//								}
//								
//								@Override
//								public PublicKey getPublic() throws IOException {
//									// TODO Auto-generated method stub
//									return null;
//								}
//								
//								@Override
//								public PrivateKey getPrivate() throws IOException {
//									// TODO Auto-generated method stub
//									return null;
//								}
//							});
//							return true;
//						}
//						catch(UserAuthException uae) {
//						}
//						
//						// Return to main loop so getRemainingMethods is called
//						// again
//						continue;
//					}
//					// Keyboard interactive
//					// TODO
////					if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
////						final SshKeyboardInteractiveAuthenticator kbi = (SshKeyboardInteractiveAuthenticator) authenticator;
////						if (connection.authenticateWithKeyboardInteractive(username, new InteractiveCallback() {
////							@Override
////							public String[] replyToChallenge(String name, String instruction, int numPrompts,
////									String[] prompt, boolean[] echo) throws Exception {
////								return kbi.challenge(name, instruction, prompt, echo);
////							}
////						})) {
////							// Authenticated!
////							return true;
////						}
////						continue;
////					}
//				}
//			}
//			return false;
//		}
		return methods;
	}

	SSHClient getSsh() {
		return ssh;
	}
}
