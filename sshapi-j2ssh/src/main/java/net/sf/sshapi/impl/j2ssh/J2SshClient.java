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
package net.sf.sshapi.impl.j2ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.sshtools.j2ssh.ScpClient;
import com.sshtools.j2ssh.SshEventAdapter;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.KBIAuthenticationClient;
import com.sshtools.j2ssh.authentication.KBIPrompt;
import com.sshtools.j2ssh.authentication.KBIRequestHandler;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationPrompt;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.IOChannel;
import com.sshtools.j2ssh.forwarding.ForwardingClient;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.forwarding.ForwardingConfigurationException;
import com.sshtools.j2ssh.forwarding.XDisplay;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractForwardingChannel;
import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshCustomChannel;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshProxyServerDetails;
import net.sf.sshapi.SshSCPClient;
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

class J2SshClient extends AbstractClient {

	protected final static class ForwardingChannel extends AbstractForwardingChannel<J2SshClient> {
		private RemoteSocketChannel localForward;

		protected ForwardingChannel(J2SshClient client, SshProvider provider, SshConfiguration configuration,
				String hostname, int port) {
			super(client, provider, configuration, hostname, port);
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (localForward == null)
				throw new IOException("Not open.");
			return new EventFiringInputStream(localForward.getInputStream(), SshDataListener.RECEIVED);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (localForward == null)
				throw new IOException("Not open.");
			return new EventFiringOutputStream(localForward.getOutputStream());
		}

		@Override
		protected final void onOpenStream() throws net.sf.sshapi.SshException {
			if (!client.con.isConnected()) {
				throw new net.sf.sshapi.SshException("SSH client is not connected");
			}
			try {
				if (!client.con.openChannel(localForward = new RemoteSocketChannel(hostname, port))) {
					throw new net.sf.sshapi.SshException("Failed to open direct-tcpip channel.");
				}
			} catch (IOException ioe) {
				throw new net.sf.sshapi.SshException("Failed to open direct-tcpip channel.");
			}
		}

		@Override
		protected void onCloseStream() throws net.sf.sshapi.SshException {
			try {
				localForward.close();
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR,
						"Failed to close local forward.", e);
			}
		}
	}

	class HostKeyVerificationBridge implements HostKeyVerification {
		@Override
		public boolean verifyHost(final String host, final SshPublicKey pk) {
			SshConfiguration configuration = getConfiguration();
			if (configuration.getHostKeyValidator() != null) {
				int status;
				try {
					status = configuration.getHostKeyValidator().verifyHost(new AbstractHostKey() {

						@Override
						public String getFingerprint() {
							String v = pk.getFingerprint();
							int idx = v.indexOf(' ');
							if (idx == -1)
								throw new IllegalStateException("Unexpected fingerprint format.");
							v = v.substring(idx + 1);
							String[] parts = v.split(" ");
							for (int i = 0; i < parts.length; i++) {
								if (parts[i].length() < 2)
									parts[i] = "0" + parts[i];
							}
							return String.join(":", parts);
						}

						@Override
						public String getHost() {
							return host;
						}

						@Override
						public byte[] getKey() {
							return pk.getEncoded();
						}

						@Override
						public String getType() {
							return pk.getAlgorithmName();
						}

						@Override
						public int getBits() {
							return pk.getBitLength();
						}
					});
					return status == SshHostKeyValidator.STATUS_HOST_KEY_VALID;
				} catch (net.sf.sshapi.SshException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("The authenticity of host '" + host + "' can't be established.");
				System.out.println(pk.getAlgorithmName() + " key fingerprint is " + pk.getFingerprint());
				return Util.promptYesNo("Are you sure you want to continue connecting?");
			}
			return false;
		}
	}

	class J2SSHSCPClient extends AbstractSCPClient {
		private ScpClient client;

		J2SSHSCPClient() {
			super(getProvider());
		}

		@Override
		public void doPut(String remotePath, String mode, File sourceFile, boolean recursive)
				throws net.sf.sshapi.SshException {
			try {
				client.put(sourceFile.getAbsolutePath(), remotePath, recursive);
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}
		}

		@Override
		public void get(String remoteFilePath, File destinationFile, boolean recursive)
				throws net.sf.sshapi.SshException {
			try {
				client.get(destinationFile.getAbsolutePath(), remoteFilePath, recursive);
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}
		}

		@Override
		protected void onOpen() throws net.sf.sshapi.SshException {
			try {
				client = con.openScpClient();
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}
			super.onOpen();
		}
	}

	static class RemoteSocketChannel extends IOChannel {
		private String hostname;
		private int port;

		RemoteSocketChannel(String hostname, int port) {
			this.hostname = hostname;
			this.port = port;
		}

		@Override
		public byte[] getChannelConfirmationData() {
			return null;
		}

		@Override
		public byte[] getChannelOpenData() {
			try {
				@SuppressWarnings("resource")
				ByteArrayWriter baw = new ByteArrayWriter();
				baw.writeString(hostname);
				baw.writeInt(port);
				baw.writeString("127.0.0.1");
				baw.writeInt(0);
				return baw.toByteArray();
			} catch (IOException ioe) {
				return null;
			}
		}

		@Override
		public String getChannelType() {
			return "direct-tcpip";
		}

		@Override
		protected int getMaximumPacketSize() {
			return 32768;
		}

		@Override
		protected int getMaximumWindowSpace() {
			return 131072;
		}

		@Override
		protected int getMinimumWindowSpace() {
			return 32768;
		}

		@Override
		protected void onChannelOpen() throws IOException {
		}

		@Override
		protected void onChannelRequest(String arg0, boolean arg1, byte[] arg2) throws IOException {
			connection.sendChannelRequestFailure(this);
		}
	}

	private static int fwdName = 0;
	private final com.sshtools.j2ssh.SshClient con;
	private ForwardingClient forwarding;
	private int timeout = 0;
	private String username;

	public J2SshClient(SshConfiguration configuration) throws SshException {
		super(configuration);
		con = new com.sshtools.j2ssh.SshClient();
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws net.sf.sshapi.SshException {
		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
		try {
			@SuppressWarnings("unchecked")
			String[] methods = (String[]) con.getAvailableAuthMethods(username).toArray(new String[0]);
			for (int i = 0; i < methods.length && !con.isAuthenticated(); i++) {
				// Now at least one authentication method has been tried (none),
				// we
				// can get the banner
				SshAuthenticator authenticator = authenticatorMap.get(methods[i]);
				if (authenticator != null) {
					int result;
					try {
						interruptable();
						result = con.authenticate(createAuthentication(authenticator, methods[i]));
					} finally {
						uninterruptable();
					}
					switch (result) {
					case AuthenticationProtocolState.COMPLETE:
						if (i == 0) {
							checkBanner();
						}
						break;
					case AuthenticationProtocolState.FAILED:
						return false;
					case AuthenticationProtocolState.PARTIAL:
						if (i == 0) {
							checkBanner();
						}
						continue;
					case AuthenticationProtocolState.CANCELLED:
						throw new net.sf.sshapi.SshException("Authentication cancelled.");
					case AuthenticationProtocolState.READY:
						if (i == 0) {
							checkBanner();
						}
						break;
					default:
						throw new net.sf.sshapi.SshException("Unknown authentication result " + result + ".");
					}
				}
			}
		} catch (SshException sshe) {
			throw new net.sf.sshapi.SshException("Failed to authenticate.", sshe);
		} catch (net.sf.sshapi.SshException sshe) {
			throw sshe;
		} catch (IOException ioe) {
			if (ioe.getCause() instanceof InvalidSshKeyException) {
				/* Bad password for key? */
				return false;
			}
			throw new net.sf.sshapi.SshException("I/O error.", ioe);
		}
		if (con.isAuthenticated()) {
			forwarding = con.getForwardingClient();
			if (getConfiguration().getX11Host() != null) {
				XDisplay xd = new XDisplay(getConfiguration().getX11Host(), getConfiguration().getX11Screen());
				forwarding.enableX11Forwarding(xd);
			}
			return true;
		}
		return false;
	}

	@Override
	protected SshPortForward doCreateLocalForward(String localAddress, int localPort, String remoteHost, int remotePort)
			throws net.sf.sshapi.SshException {
		if (localPort == 0) {
			localPort = Util.findRandomPort();
		}
		final ForwardingConfiguration fwd = new ForwardingConfiguration("FWD" + (fwdName++), localAddress, localPort,
				remoteHost, remotePort);
		return new AbstractPortForward(getProvider()) {

			@Override
			public int getBoundPort() {
				return fwd.getPortToBind();
			}

			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.stopLocalForwarding(fwd.getName());
					forwarding.removeLocalForwarding(fwd.getName());
				} catch (ForwardingConfigurationException e) {
					throw new net.sf.sshapi.SshException("Failed to stop local port forward.", e);
				}
			}

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					forwarding.addLocalForwarding(fwd);
					forwarding.startLocalForwarding(fwd.getName());
				} catch (ForwardingConfigurationException e) {
					throw new net.sf.sshapi.SshException("Failed to start local port forward.", e);
				}
			}
		};
	}

	@Override
	protected SshPortForward doCreateRemoteForward(String remoteHost, int remotePort, String localAddress,
			int localPort) throws net.sf.sshapi.SshException {
		final ForwardingConfiguration fwd = new ForwardingConfiguration("FWD" + (fwdName++), remoteHost, remotePort,
				localAddress, localPort);
		return new AbstractPortForward(getProvider()) {
			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.stopRemoteForwarding(fwd.getName());
					forwarding.removeRemoteForwarding(fwd.getName());
				} catch (IOException e) {
					throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR,
							"Failed to stop remote port forward.", e);
				}
			}

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					forwarding.addRemoteForwarding(fwd);
					forwarding.startRemoteForwarding(fwd.getName());
				} catch (ForwardingConfigurationException e) {
					throw new net.sf.sshapi.SshException("Failed to start remote port forward.", e);
				} catch (IOException e) {
					throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR,
							"Failed to start remote port forward.", e);
				}
			}
		};
	}

	@Override
	protected SshShell doCreateShell(String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight,
			byte[] terminalModes) throws net.sf.sshapi.SshException {
		try {
			SessionChannelClient session = con.openSessionChannel();
			requestPty(termType, colWidth, rowHeight, pixWidth, pixHeight, terminalModes, session);
			return new J2SshShell(getProvider(), getConfiguration(), session);
		} catch (net.sf.sshapi.SshException e) {
			throw e;
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
		}
	}

	@Override
	public int getChannelCount() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected.");
		}
		return con.getActiveChannelCount();
	}

	@Override
	protected SshCustomChannel doCreateForwardingChannel(String hostname, int port) throws net.sf.sshapi.SshException {
		return new ForwardingChannel(this, getProvider(), getConfiguration(), hostname, port);
	}

	@Override
	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return con.getServerId();
	}

	@Override
	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// J2SSH only supports SSH2
		return SshConfiguration.SSH2_ONLY;
	}

	@Override
	public int getTimeout() throws IOException {
		return timeout;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && con.isAuthenticated();
	}

	@Override
	public boolean isConnected() {
		return con != null && con.isConnected();
	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		this.timeout = timeout;
		if (con != null) {
			con.setSocketTimeout(timeout);
		}
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws net.sf.sshapi.SshException {
		try {
			SshConnectionProperties properties = new SshConnectionProperties();
			properties.setHost(hostname);
			properties.setPort(port);
			properties.setForwardingAutoStartMode(true);
			con.setSocketTimeout(timeout);
			SshConfiguration configuration = getConfiguration();
			configureAlgorithms(properties, configuration);
			SshProxyServerDetails proxyServer = configuration.getProxyServer();
			if (proxyServer != null) {
				properties.setTransportProviderString(proxyServer.getType().toString());
				properties.setProxyHost(proxyServer.getHostname());
				properties.setProxyPort(proxyServer.getPort());
				properties.setProxyUsername(proxyServer.getUsername());
				properties.setProxyPassword(new String(proxyServer.getPassword()));
			}
			con.addEventHandler(new SshEventAdapter() {

				@Override
				public void onDisconnect(TransportProtocol transport) {
					SshConfiguration.getLogger().info("Disconnect for {0}", transport.getConnectionId());
					if(!isAuthenticated())
						interrupt();
				}

				@Override
				public void onSocketTimeout(TransportProtocol transport) {
					SshConfiguration.getLogger().info("Socket timeout for {0}", transport.getConnectionId());
					if(!isAuthenticated())
						interrupt();
				}
				
			});
			try {
				interruptable();
				con.connect(hostname, port, new HostKeyVerificationBridge());
			} finally {
				uninterruptable();
			}
			this.username = username;
		} catch (IOException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	@Override
	protected SshCommand doCreateCommand(final String command, String termType, int colWidth, int rowHeight,
			int pixWidth, int pixHeight, byte[] terminalModes) throws net.sf.sshapi.SshException {
		try {
			final SessionChannelClient session = con.openSessionChannel();
			requestPty(termType, colWidth, rowHeight, pixWidth, pixHeight, terminalModes, session);
			return new J2SshCommandChannel(getProvider(), getConfiguration(), session) {
				@Override
				public void onChannelOpen() throws net.sf.sshapi.SshException {
					try {
						if (!session.executeCommand(command)) {
							throw new net.sf.sshapi.SshException("Failed to execute command.");
						}
					} catch (IOException e) {
						throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
					}
				}
			};
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
		}
	}

	@Override
	protected SshSCPClient doCreateSCP() throws net.sf.sshapi.SshException {
		return new J2SSHSCPClient();
	}

	@Override
	protected SftpClient doCreateSftp() throws net.sf.sshapi.SshException {
		return new J2SshSftpClient(this, con);
	}

	@Override
	protected void onClose() throws net.sf.sshapi.SshException {
		if (!isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.NOT_OPEN, "Not connected.");
		}
		con.disconnect();
	}

	private String checkBanner() throws IOException {
		String banner = con.getAuthenticationBanner(2000);
		if (banner != null && !banner.trim().equals("") && getConfiguration().getBannerHandler() != null) {
			getConfiguration().getBannerHandler().banner(banner);
		}
		return banner;
	}

	private void configureAlgorithms(SshConnectionProperties properties, SshConfiguration configuration) {
		if (configuration.getPreferredClientToServerCipher() != null) {
			properties.setPrefCSEncryption(configuration.getPreferredClientToServerCipher());
		}
		if (configuration.getPreferredServerToClientCipher() != null) {
			properties.setPrefSCEncryption(configuration.getPreferredServerToClientCipher());
		}
		if (configuration.getPreferredClientToServerMAC() != null) {
			properties.setPrefCSMac(configuration.getPreferredClientToServerMAC());
		}
		if (configuration.getPreferredServerToClientMAC() != null) {
			properties.setPrefSCMac(configuration.getPreferredServerToClientMAC());
		}
		if (configuration.getPreferredClientToServerCompression() != null) {
			properties.setPrefCSComp(configuration.getPreferredClientToServerCompression());
		}
		if (configuration.getPreferredServerToClientCompression() != null) {
			properties.setPrefSCComp(configuration.getPreferredServerToClientCompression());
		}
		if (configuration.getPreferredKeyExchange() != null) {
			properties.setPrefKex(configuration.getPreferredKeyExchange());
		}
		if (configuration.getPreferredPublicKey() != null) {
			properties.setPrefPublicKey(configuration.getPreferredPublicKey());
		}
	}

	private SshAuthenticationClient createAuthentication(final SshAuthenticator authenticator, String type)
			throws AuthenticationProtocolException {
		if (authenticator instanceof SshPasswordAuthenticator) {
			final SshPasswordAuthenticator passwordAuthenticator = (SshPasswordAuthenticator) authenticator;
			PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
			pac.setUsername(username);
			pac.setAuthenticationPrompt(new SshAuthenticationPrompt() {
				@Override
				public boolean showPrompt(SshAuthenticationClient sshac) throws AuthenticationProtocolException {
					if (sshac instanceof PasswordAuthenticationClient) {
						char[] pw = passwordAuthenticator.promptForPassword(J2SshClient.this, "Password");
						if (pw == null) {
							return false;
						}
						PasswordAuthenticationClient pac = (PasswordAuthenticationClient) sshac;
						if (pac.getUsername() == null) {
							pac.setUsername(username);
						}
						pac.setPassword(new String(pw));
						return true;
					} else {
						throw new AuthenticationProtocolException("PasswordAuthenticationClient instance required");
					}
				}
			});
			return pac;
		}
		if (authenticator instanceof SshPublicKeyAuthenticator) {
			final SshPublicKeyAuthenticator publicKeyAuthenticator = (SshPublicKeyAuthenticator) authenticator;
			PublicKeyAuthenticationClient pkc = new PublicKeyAuthenticationClient();
			pkc.setUsername(username);
			pkc.setAuthenticationPrompt(new SshAuthenticationPrompt() {
				@Override
				public boolean showPrompt(SshAuthenticationClient sshac) throws AuthenticationProtocolException {
					if (sshac instanceof PublicKeyAuthenticationClient) {
						try {
							SshPrivateKeyFile pkf = SshPrivateKeyFile.parse(publicKeyAuthenticator.getPrivateKey());
							char[] pw = null;
							if (pkf.isPassphraseProtected()) {
								pw = publicKeyAuthenticator.promptForPassphrase(J2SshClient.this, "Passphrase");
								if (pw == null) {
									return false;
								}
							}
							PublicKeyAuthenticationClient pac = (PublicKeyAuthenticationClient) sshac;
							if (pac.getUsername() == null) {
								pac.setUsername(username);
							}
							pac.setKey(pkf.toPrivateKey(pw == null ? "" : new String(pw)));
						} catch (InvalidSshKeyException e) {
							if(SshConfiguration.getLogger().isDebug())
								SshConfiguration.getLogger().log(Level.DEBUG, "Failed to authenticate.", e);
							AuthenticationProtocolException ape = new AuthenticationProtocolException(
									e.getLocalizedMessage());
							ape.initCause(e);
							throw ape;
						}
						return true;
					} else {
						throw new AuthenticationProtocolException("PasswordAuthenticationClient instance required");
					}
				}
			});
			return pkc;
		}
		if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
			final SshKeyboardInteractiveAuthenticator keyboardInteractiveAuthenticator = (SshKeyboardInteractiveAuthenticator) authenticator;
			KBIAuthenticationClient kbic = new KBIAuthenticationClient();
			kbic.setUsername(username);
			kbic.setKBIRequestHandler(new KBIRequestHandler() {
				@Override
				public void showPrompts(String name, String instruction, KBIPrompt[] prompts) {
					// Why getting null prompt?
					if (prompts != null) {
						String[] prompt = new String[prompts.length];
						boolean[] echo = new boolean[prompts.length];
						for (int i = 0; i < prompts.length; i++) {
							prompt[i] = prompts[i].getPrompt();
							echo[i] = prompts[i].echo();
						}
						String[] answers = keyboardInteractiveAuthenticator.challenge(name, instruction, prompt, echo);
						if (answers != null) {
							for (int i = 0; i < prompts.length; i++) {
								prompts[i].setResponse(answers[i]);
							}
						}
					}
				}
			});
			return kbic;
		}
		throw new UnsupportedOperationException();
	}

	private void requestPty(String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight,
			byte[] terminalModes, SessionChannelClient session)
			throws UnsupportedEncodingException, IOException, net.sf.sshapi.SshException {
		if (termType != null) {
			if (terminalModes == null || terminalModes.length == 0) {
				terminalModes = new byte[] { 0 };
			}
			String modes = new String(terminalModes, "US-ASCII");
			if (!session.requestPseudoTerminal(termType, colWidth, rowHeight, pixWidth, pixHeight, modes)) {
				throw new net.sf.sshapi.SshException("Failed to allocate pseudo tty.");
			}
		}
	}
}
