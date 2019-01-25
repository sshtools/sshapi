/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.impl.maverickng;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.sshtools.client.AbstractKeyboardInteractiveCallback;
import com.sshtools.client.BannerDisplay;
import com.sshtools.client.ClientAuthenticator;
import com.sshtools.client.ClientStateListener;
import com.sshtools.client.ConnectionProtocolClient;
import com.sshtools.client.KeyboardInteractiveAuthenticator;
import com.sshtools.client.KeyboardInteractivePrompt;
import com.sshtools.client.KeyboardInteractivePromptCompletor;
import com.sshtools.client.PasswordAuthenticator;
import com.sshtools.client.PublicKeyAuthenticator;
import com.sshtools.client.SshClientContext;
import com.sshtools.client.UnauthorizedException;
import com.sshtools.common.knownhosts.HostKeyVerification;
import com.sshtools.common.nio.SshEngine;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.ssh.Connection;
import com.sshtools.common.ssh.ForwardingPolicy;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.forwarding.AbstractPortForward;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.Util;

class MaverickNGSshClient extends AbstractClient
// implements ForwardingClientListener
{
	// private ForwardingClient forwarding;
	// private Map forwardingChannels = new HashMap();
	private SshEngine engine;
	private SshClientContext sshContext;
	private Connection<SshClientContext> connection;
	private String hostname;
	private int port;
	private Semaphore auth = new Semaphore(1);
	private SshAuthenticator[] authenticators;

	MaverickNGSshClient(SshEngine engine, SshConfiguration configuration) throws SshException, IOException {
		super(configuration);
		this.engine = engine;
		sshContext = new SshClientContext(engine);
		sshContext.setHostKeyVerification(new HostKeyVerificationBridge());
		sshContext.addStateListener(new ClientStateListener() {
			
			@Override
			public void disconnected(Connection<SshClientContext> con) {
				connection = null;
			}
			
			@Override
			public void connected(Connection<SshClientContext> con) {
				connection = con;
				
			}
			
			@Override
			public void authenticate(Connection<SshClientContext> arg0, Set<String> arg1, boolean arg2,
					List<ClientAuthenticator> arg3) {
				try {
					/* Will block until SshClient.authenticate() is called and the authenticators are supplied */
					auth.acquire();
				}
				catch(InterruptedException ie) {
					throw new IllegalStateException("Interrupted waiting for authentication.");
				}
				
				/* Client code has called SshClient.authenticate(), we now have authenticators */
				
			}
		});
		
		// Version
		if (configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY)
			throw new IllegalArgumentException("SSH1 is not supported by this provider.");
		if (configuration.getPreferredClientToServerCipher() != null) {
			sshContext.setPreferredCipherCS(configuration.getPreferredClientToServerCipher());
		}
		if (configuration.getPreferredServerToClientCipher() != null) {
			sshContext.setPreferredCipherSC(configuration.getPreferredServerToClientCipher());
		}
		if (configuration.getPreferredClientToServerMAC() != null) {
			sshContext.setPreferredMacCS(configuration.getPreferredClientToServerMAC());
		}
		if (configuration.getPreferredServerToClientMAC() != null) {
			sshContext.setPreferredMacSC(configuration.getPreferredServerToClientMAC());
		}
		if (configuration.getPreferredClientToServerCompression() != null) {
			sshContext.setPreferredCompressionCS(configuration.getPreferredClientToServerCompression());
		}
		if (configuration.getPreferredServerToClientCompression() != null) {
			sshContext.setPreferredCompressionCS(configuration.getPreferredServerToClientCompression());
		}
		if (configuration.getPreferredKeyExchange() != null) {
			sshContext.setPreferredKeyExchange(configuration.getPreferredKeyExchange());
		}
		if (configuration.getPreferredPublicKey() != null) {
			// TODO
			// throw new UnsupportedOperationException();
		}
		sshContext.setBannerDisplay(new BannerDisplayBridge());
		// TODO expose as configurable
		sshContext.setForwardingPolicy(new ForwardingPolicy() {
			@Override
			public boolean checkInterfacePermitted(Connection<?> con, String originHost, int originPort) {
				return true;
			}

			@Override
			public boolean checkHostPermitted(Connection<?> con, String host, int port) {
				return true;
			}
		});
	}

	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws net.sf.sshapi.SshException {
		sshContext.setUsername(username);
		this.hostname = hostname;
		this.port = port;
		
		try {
			if(authenticators.length == 0) {
				/* For separate authentication */
				auth.acquire();
			}
			else {
				/* For pre-emptive authentication */
				for (SshAuthenticator a : authenticators) {
					sshContext.addAuthenticator(createAuthentication(a, ""));
				}
			}
			
			engine.connect(hostname, port, sshContext);
		} catch (IOException sshe) {
			auth.release();
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, sshe);
		} catch (SshException e) {
			auth.release();
			throw new net.sf.sshapi.SshException("Failed to authenticate.", e);
		} catch (InterruptedException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}
	}

	public boolean isConnected() {
		return connection != null;
	}

	public boolean isAuthenticated() {
		return isConnected() && connection.isAuthenticated();
	}

	@Override
	public int getRemoteProtocolVersion() {
		return SshConfiguration.SSH2_ONLY;
	}

	// private String[] getAuthenticationMethods() throws SshException {
	// String[] authenticationMethods =
	// client.getAuthenticationMethods(client.getUsername());
	// return authenticationMethods;
	// }
	@Override
	public SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws net.sf.sshapi.SshException {
		synchronized (sshContext) {
			return new MaverickNGSshShell(connection, termType, cols, rows, pixWidth, pixHeight, terminalModes);
		}
	}

	@Override
	public SftpClient createSftp() throws net.sf.sshapi.SshException {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected.");
		}
		return new MaverickNGSftpClient(connection);
	}

	public int getChannelCount() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected.");
		}
		return connection.getConnectionProtocol().getActiveChannels().size();
	}

	public SshPortForward createRemoteForward(final String remoteHost, final int remotePort, final String localAddress,
			final int localPort) throws net.sf.sshapi.SshException {
		ConnectionProtocolClient client = (ConnectionProtocolClient) connection.getConnectionProtocol();
		final String fRemoteHost = remoteHost == null ? "0.0.0.0" : remoteHost;
		return new AbstractPortForward() {
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					client.startRemoteForwarding(fRemoteHost, remotePort, localAddress, localPort);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to start remote forward.", e);
				}
			}

			protected void onClose() throws net.sf.sshapi.SshException {
				// TODO Cant stop a remote forwarding?
				// try {
				// client.stopRemoteForwarding(fRemoteHost, remotePort);
				// } catch (SshException e) {
				// throw new net.sf.sshapi.SshException("Failed to stop remote
				// forward.", e);
				// }
			}
		};
	}

	public SshPortForward createLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws net.sf.sshapi.SshException {
		ConnectionProtocolClient client = (ConnectionProtocolClient) connection.getConnectionProtocol();
		final String fLocalAddress = localAddress == null ? "0.0.0.0" : localAddress;
		return new AbstractPortForward() {
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					client.startLocalForwarding(fLocalAddress, localPort, remoteHost, remotePort);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to start local forward.", e);
				} catch (UnauthorizedException e) {
					throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.UNAUTHORIZED,
							"Not authorized to start forward.", e);
				}
			}

			protected void onClose() throws net.sf.sshapi.SshException {
				client.stopLocalForwarding(fLocalAddress + ":" + localPort);
			}
		};
	}

	public boolean authenticate(SshAuthenticator... authenticators) throws net.sf.sshapi.SshException {
		if(this.authenticators != null) {
			throw new IllegalStateException("Already authenticating");
		}
		this.authenticators = authenticators;
		
		/* Release auth semaphore, this will either unblock the wait in ClientStateListener.authenticate() 
		 * or allow it to just continue if it hasn't reached that point yet. 
		 */
		auth.release();
		
//		if (authSemaphore.availablePermits() < 1)
//			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, "Already authenticating.");
//		try {
//			authSemaphore.acquire();
//			authSemaphore.acquire();
//			authSemaphore.release();
//		} catch (IOException sshe) {
////			authSemaphore.release();
//			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, sshe);
//		} catch (SshException e) {
////			authSemaphore.release();
//			throw new net.sf.sshapi.SshException("Failed to authenticate.", e);
//		} catch (InterruptedException e) {
//			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
//		}
		// if (isAuthenticated()) {
		// forwarding = new ForwardingClient(client);
		// forwarding.addListener(this);
		// SshConfiguration configuration = getConfiguration();
		// return true;
		// }
		// return false;
		return isAuthenticated();
	}

	private ClientAuthenticator createAuthentication(final SshAuthenticator authenticator, String type)
			throws net.sf.sshapi.SshException {
		// PrivateKeyFileAuthenticator pfa;
		if (authenticator instanceof SshPasswordAuthenticator) {
			return new PasswordAuthenticator() {
				@Override
				public String getPassword() {
					char[] answer = ((SshPasswordAuthenticator) authenticator)
							.promptForPassword(MaverickNGSshClient.this, "Password");
					if(answer == null && auth.availablePermits() < 1)
						auth.release();
					return answer == null ? null : new String(answer);
				}
			};
		} else if (authenticator instanceof SshPublicKeyAuthenticator) {
			SshPublicKeyAuthenticator pk = (SshPublicKeyAuthenticator) authenticator;
			try {
				SshPrivateKeyFile pkf = SshPrivateKeyFileFactory.parse(pk.getPrivateKey());
				SshKeyPair pair = null;
				for (int i = 2; i >= 0; i--) {
					if (pkf.isPassphraseProtected()) {
						char[] pa = pk.promptForPassphrase(MaverickNGSshClient.this, "Passphrase");
						if (pa == null) {
							throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.AUTHENTICATION_CANCELLED);
						}
						try {
							pair = pkf.toKeyPair(new String(pa));
						} catch (InvalidPassphraseException ipe) {
							if (i == 0) {
								throw new net.sf.sshapi.SshException(
										net.sf.sshapi.SshException.AUTHENTICATION_ATTEMPTS_EXCEEDED);
							}
						}
					} else {
						try {
							pair = pkf.toKeyPair("");
						} catch (InvalidPassphraseException ipe) {
							throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.AUTHENTICATION_FAILED, ipe);
						}
					}
				}
				return new PublicKeyAuthenticator(pair);
			} catch (IOException ioe) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, ioe);
			}
		} else if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
			final SshKeyboardInteractiveAuthenticator kbi = (SshKeyboardInteractiveAuthenticator) authenticator;
			return new KeyboardInteractiveAuthenticator(new AbstractKeyboardInteractiveCallback() {
				@Override
				public void showPrompts(String name, String instruction, KeyboardInteractivePrompt[] prompts,
						KeyboardInteractivePromptCompletor keyboardInteractivePromptCompletor) {
					String[] prompt = new String[prompts.length];
					boolean[] echo = new boolean[prompts.length];
					for (int i = 0; i < prompts.length; i++) {
						prompt[i] = prompts[i].getPrompt();
						echo[i] = prompts[i].echo();
					}
					String[] answers = kbi.challenge(name, instruction, prompt, echo);
					if (answers != null) {
						for (int i = 0; i < prompts.length; i++) {
							prompts[i].setResponse(answers[i]);
						}
						return;
					}
					if(auth.availablePermits() < 1)
						auth.release();
					throw new IllegalStateException("Cancelled.");
				}
			});
		}
		throw new UnsupportedOperationException(
				String.format("Authenticators of type %s are not supported.", authenticator.getClass()));
	}

	// public SshShell createShell(String termType, int colWidth, int rowHeight,
	// int
	// pixWidth, int pixHeight,
	// byte[] terminalModes) throws net.sf.sshapi.SshException {
	// try {
	// SshConfiguration.getLogger().log(Level.DEBUG, "Opening session channel");
	// com.maverick.ssh.SshSession session = client.openSessionChannel();
	// if (termType != null) {
	// SshConfiguration.getLogger().log(Level.DEBUG, "Requesting pty for " +
	// termType + " " + colWidth + "x"
	// + rowHeight + " [" + pixWidth + "x" + pixHeight + "] = " +
	// terminalModes);
	// requestPty(client, termType, colWidth, rowHeight, pixWidth, pixHeight,
	// terminalModes, session);
	// }
	// return new MaverickNGSshShell(session);
	// } catch (net.sf.sshapi.SshException sshe) {
	// throw sshe;
	// } catch (Exception e) {
	// throw new net.sf.sshapi.SshException("Failed to open session channel.",
	// e);
	// }
	// }
	//
	// private void requestPty(SshClient client, String termType, int colWidth,
	// int
	// rowHeight, int pixWidth, int pixHeight,
	// byte[] terminalModes, com.maverick.ssh.SshSession session) throws
	// net.sf.sshapi.SshException {
	// try {
	// PseudoTerminalModes ptm = new PseudoTerminalModes(client);
	// if (terminalModes != null) {
	// for (int i = 0; i < terminalModes.length; i++) {
	// ptm.setTerminalMode(terminalModes[i], true);
	// }
	// }
	// if (!session.requestPseudoTerminal(termType, colWidth, rowHeight,
	// pixWidth,
	// pixHeight, ptm)) {
	// throw new net.sf.sshapi.SshException("Failed to allocate pseudo tty.");
	// }
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException(e);
	// }
	// }
	//
	// public SshCommand createCommand(final String command) throws
	// net.sf.sshapi.SshException {
	// try {
	// final com.maverick.ssh.SshSession session = client.openSessionChannel();
	// return new MaverickNGSshStreamChannel(session) {
	//
	// public void onChannelOpen() throws net.sf.sshapi.SshException {
	// try {
	// session.executeCommand(command);
	// } catch (Exception e) {
	// throw new net.sf.sshapi.SshException("Failed to open session channel.",
	// e);
	// }
	// }
	// };
	// } catch (Exception e) {
	// throw new net.sf.sshapi.SshException("Failed to open session channel.",
	// e);
	// }
	// }
	public String getUsername() {
		return sshContext == null ? null : sshContext.getUsername();
	}

	// public SshPortForward createLocalForward(final String localAddress, final
	// int
	// localPort, final String remoteHost,
	// final int remotePort) throws net.sf.sshapi.SshException {
	//
	// final String fLocalAddress = localAddress == null ? "0.0.0.0" :
	// localAddress;
	// return new AbstractPortForward() {
	//
	// protected void onOpen() throws net.sf.sshapi.SshException {
	// try {
	// forwarding.startLocalForwarding(fLocalAddress, localPort, remoteHost,
	// remotePort);
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException("Failed to start local forward.",
	// e);
	// }
	// }
	//
	// protected void onClose() throws net.sf.sshapi.SshException {
	// try {
	// forwarding.stopLocalForwarding(fLocalAddress + ":" + localPort, true);
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException("Failed to stop local forward.", e);
	// }
	// }
	// };
	// }
	//
	// public SshPortForward createRemoteForward(final String remoteHost, final
	// int
	// remotePort, final String localAddress,
	// final int localPort) throws net.sf.sshapi.SshException {
	//
	// final String fRemoteHost = remoteHost == null ? "0.0.0.0" : remoteHost;
	// return new AbstractPortForward() {
	//
	// protected void onOpen() throws net.sf.sshapi.SshException {
	// try {
	// forwarding.requestRemoteForwarding(fRemoteHost, remotePort, localAddress,
	// localPort);
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException("Failed to start remote forward.",
	// e);
	// }
	// }
	//
	// protected void onClose() throws net.sf.sshapi.SshException {
	// try {
	// forwarding.cancelRemoteForwarding(fRemoteHost, remotePort);
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException("Failed to stop remote forward.",
	// e);
	// }
	// }
	// };
	// }
	public void disconnect() throws net.sf.sshapi.SshException {
		if (!isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.NOT_OPEN, "Not connected.");
		}
		try {
			connection.disconnect();
		} finally {
			if (auth.availablePermits() < 1)
				auth.release();
			
			// if (forwarding != null) {
			// forwarding.removeListener(this);
			// }
		}
	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		sshContext.setIdleConnectionTimeoutSeconds(timeout);
	}

	@Override
	public int getTimeout() throws IOException {
		return sshContext.getIdleConnectionTimeoutSeconds();
	}

	public String getRemoteIdentification() {
		if (!isConnected()) {
			/*
			 * BUG: Maverick NG does not have a separate connection / authentication phase,
			 * so we can't do this
			 */
			return "Unknown";
		}
		return connection.getRemoteIdentification();
	}

	class HostKeyVerificationBridge implements HostKeyVerification {
		public boolean verifyHost(final String host, final SshPublicKey pk) throws SshException {
			if (getConfiguration().getHostKeyValidator() != null) {
				int status;
				try {
					status = getConfiguration().getHostKeyValidator().verifyHost(new AbstractHostKey() {
						public String getType() {
							return pk.getAlgorithm();
						}

						public byte[] getKey() {
							try {
								return pk.getEncoded();
							} catch (SshException e) {
								throw new RuntimeException(e);
							}
						}

						public String getHost() {
							return host;
						}

						public String getFingerprint() {
							try {
								return pk.getFingerprint();
							} catch (SshException e) {
								throw new RuntimeException(e);
							}
						}
					});
					return status == SshHostKeyValidator.STATUS_HOST_KEY_VALID;
				} catch (net.sf.sshapi.SshException e) {
					SshConfiguration.getLogger().log(Level.ERROR, "Failed to verify host key.", e);
				}
			} else {
				System.out.println("The authenticity of host '" + host + "' can't be established.");
				System.out.println(pk.getAlgorithm() + " key fingerprint is " + pk.getFingerprint());
				return Util.promptYesNo("Are you sure you want to continue connecting?");
			}
			return false;
		}
	}

	class BannerDisplayBridge implements BannerDisplay {
		public void displayBanner(String message) {
			getConfiguration().getBannerHandler().banner(message);
		}
	}

	// public void channelClosed(int type, String key, SshTunnel tunnel) {
	// int sshapiType = getTypeForTunnel(type);
	// TunnelChannel channel = (TunnelChannel) forwardingChannels.get(tunnel);
	// if (channel != null) {
	// try {
	// firePortForwardChannelClosed(sshapiType, channel);
	// } finally {
	// forwardingChannels.remove(tunnel);
	// }
	// } else {
	// SshConfiguration.getLogger().log(Level.WARN,
	// "Got a channel closed event for a channel we don't know about (" + key +
	// ").");
	// }
	// }
	public void channelFailure(int type, String key, String host, int port, boolean isConnected, Throwable t) {
		// TODO?
	}

	public boolean checkLocalSourceAddress(SocketAddress arg0, String arg1, int arg2, String arg3, int arg4) {
		return true;
	}

	// public void channelOpened(int type, String key, SshTunnel tunnel) {
	// int sshapiType = getTypeForTunnel(type);
	// TunnelChannel channel = new TunnelChannel(tunnel);
	// forwardingChannels.put(tunnel, channel);
	// // The channel is actually already open, but this will set its state
	// // correctly in the wrapper
	// try {
	// channel.open();
	// } catch (net.sf.sshapi.SshException e) {
	// throw new RuntimeException(e);
	// }
	// firePortForwardChannelOpened(sshapiType, channel);
	// }
	//
	// private int getTypeForTunnel(int type) {
	// int sshapiType = SshPortForward.LOCAL_FORWARDING;
	// if (type == ForwardingClientListener.X11_FORWARDING) {
	// sshapiType = SshPortForward.X11_FORWARDING;
	// } else if (type == ForwardingClientListener.REMOTE_FORWARDING) {
	// sshapiType = SshPortForward.REMOTE_FORWARDING;
	// }
	// return sshapiType;
	// }
	public void forwardingStarted(int type, String key, String host, int port) {
	}

	public void forwardingStopped(int type, String key, String host, int port) {
	}
	// class TunnelChannel extends AbstractDataProducingComponent implements
	// SshPortForwardTunnel {
	//
	// private SshTunnel tunnel;
	//
	// public TunnelChannel(SshTunnel tunnel) {
	// this.tunnel = tunnel;
	// tunnel.addChannelEventListener(new ChannelEventListener() {
	//
	// public void extendedDataReceived(SshChannel channel, byte[] data, int
	// off,
	// int len,
	// int extendedDataType) {
	// }
	//
	// public void dataReceived(SshChannel channel, byte[] data, int off, int
	// len) {
	// fireData(SshDataListener.RECEIVED, data, off, len);
	// }
	//
	// public void dataSent(SshChannel channel, byte[] data, int off, int len) {
	// fireData(SshDataListener.SENT, data, off, len);
	// }
	//
	// public void channelOpened(SshChannel channel) {
	// }
	//
	// public void channelEOF(SshChannel channel) {
	// }
	//
	// public void channelClosing(SshChannel channel) {
	// }
	//
	// public void channelClosed(SshChannel channel) {
	// }
	// });
	// }
	//
	// public String getBindAddress() {
	// return tunnel.getListeningAddress();
	// }
	//
	// public int getBindPort() {
	// return tunnel.getListeningPort();
	// }
	//
	// public String getOriginatingAddress() {
	// return tunnel.getOriginatingHost();
	// }
	//
	// public int getOriginatingPort() {
	// return tunnel.getOriginatingPort();
	// }
	//
	// public String toString() {
	// return "TunnelChannel [getBindAddress()=" + getBindAddress() + ",
	// getBindPort()=" + getBindPort()
	// + ", getOriginatingAddress()=" + getOriginatingAddress() + ",
	// getOriginatingPort()="
	// + getOriginatingPort() + "]";
	// }
	//
	// protected void onClose() throws net.sf.sshapi.SshException {
	// }
	//
	// protected void onOpen() throws net.sf.sshapi.SshException {
	// }
	//
	// }
	// class MaverickSshChannel extends AbstractDataProducingComponent
	// implements
	// net.sf.sshapi.SshChannel {
	//
	// private Ssh2Channel ssh2Channel;
	// private ChannelData channelData;
	// private String name;
	//
	// public MaverickSshChannel(String name, ChannelData channelData) {
	// this.channelData = channelData;
	// this.name = name;
	// }
	//
	// /**
	// * Inform all listeners the channel has reached EOF.
	// */
	// protected void fireEof() {
	// if (getListeners() != null) {
	// for (Iterator i = new ArrayList(getListeners()).iterator(); i.hasNext();)
	// {
	// ((SshChannelListener) i.next()).eof(this);
	// }
	// }
	// }
	//
	// /**
	// * Inform all listeners a request was received.
	// *
	// * @param requestType
	// * request type
	// * @param wantReply
	// * remote side wanted reply
	// * @param data
	// * data
	// * @return send error reply
	// */
	// protected boolean fireRequest(String requestType, boolean wantReply,
	// byte[]
	// data) {
	// boolean send = false;
	// if (getListeners() != null) {
	// for (Iterator i = new ArrayList(getListeners()).iterator(); i.hasNext();)
	// {
	// if (((SshChannelListener) i.next()).request(this, requestType, wantReply,
	// data)) {
	// send = true;
	// }
	// }
	// }
	// return send;
	// }
	//
	// public void setSourceChannel(Ssh2Channel ssh2Channel) {
	// this.ssh2Channel = ssh2Channel;
	// ssh2Channel.addChannelEventListener(new ChannelEventListener() {
	// public void extendedDataReceived(SshChannel arg0, byte[] buf, int off,
	// int
	// len, int arg4) {
	// fireData(SshDataListener.EXTENDED, buf, off, len);
	// }
	//
	// public void dataSent(SshChannel arg0, byte[] buf, int off, int len) {
	// fireData(SshDataListener.SENT, buf, off, len);
	// }
	//
	// public void dataReceived(SshChannel arg0, byte[] buf, int off, int len) {
	// fireData(SshDataListener.RECEIVED, buf, off, len);
	// }
	//
	// public void channelOpened(SshChannel arg0) {
	// fireOpened();
	// }
	//
	// public void channelEOF(SshChannel arg0) {
	// fireEof();
	// }
	//
	// public void channelClosing(SshChannel arg0) {
	// fireClosing();
	// }
	//
	// public void channelClosed(SshChannel arg0) {
	// fireClosed();
	// }
	// });
	// }
	//
	// public InputStream getInputStream() throws IOException {
	// return ssh2Channel.getInputStream();
	// }
	//
	// public OutputStream getOutputStream() throws IOException {
	// return ssh2Channel.getOutputStream();
	// }
	//
	// public boolean sendRequest(String requesttype, boolean wantreply, byte[]
	// requestdata)
	// throws net.sf.sshapi.SshException {
	// try {
	// return ssh2Channel.sendRequest(requesttype, wantreply, requestdata);
	// } catch (SshException e) {
	// throw new net.sf.sshapi.SshException("Failed to send request.", e);
	// }
	// }
	//
	// protected void onOpen() throws net.sf.sshapi.SshException {
	// }
	//
	// protected void onClose() throws net.sf.sshapi.SshException {
	// ssh2Channel.close();
	// }
	//
	// public ChannelData getChannelData() {
	// return channelData;
	// }
	//
	// public String getName() {
	// return name;
	// }
	//
	// }
	//
	// class MaverickChannelFactoryAdapter implements ChannelFactory {
	//
	// private SshChannelHandler sshApiFactory;
	//
	// MaverickChannelFactoryAdapter(SshChannelHandler sshApiFactory) {
	// this.sshApiFactory = sshApiFactory;
	// }
	//
	// public Ssh2Channel createChannel(final String name, byte[] requestData) {
	// final ChannelData channelData = sshApiFactory.createChannel(name,
	// requestData);
	// final MaverickSshChannel msc = new MaverickSshChannel(name, channelData);
	// Ssh2Channel ssh2Channel = new Ssh2Channel(name,
	// channelData.getWindowSize(),
	// channelData.getPacketSize()) {
	// protected byte[] create() {
	// return channelData.create();
	// }
	//
	// protected void channelRequest(String requesttype, boolean wantreply,
	// byte[]
	// requestdata)
	// throws SshException {
	// super.channelRequest(requesttype, msc.fireRequest(requesttype, wantreply,
	// requestdata),
	// requestdata);
	// }
	// };
	// msc.setSourceChannel(ssh2Channel);
	// try {
	// sshApiFactory.channelCreated(msc);
	// } catch (IOException ioe) {
	// throw new RuntimeException(ioe);
	// }
	// return ssh2Channel;
	// }
	//
	// public String[] supportedChannelTypes() {
	// return sshApiFactory.getSupportChannelNames();
	// }
	//
	// }
}
