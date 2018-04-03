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
package net.sf.sshapi.impl.j2ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.net.SocketFactory;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshExtendedStreamChannel;
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

import com.sshtools.j2ssh.ScpClient;
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
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFile;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

class J2SshClient extends AbstractClient {
	private final com.sshtools.j2ssh.SshClient con;
	private String username;
	private ForwardingClient forwarding;
	private static int fwdName = 0;
	private int timeout;

	public J2SshClient(SshConfiguration configuration) throws SshException {
		super(configuration);
		con = new com.sshtools.j2ssh.SshClient();
	}

	public void connect(String username, String hostname, int port) throws net.sf.sshapi.SshException {
		if (isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.ALREADY_OPEN, "Already connected.");
		}
		try {
			SshConnectionProperties properties = new SshConnectionProperties();
			properties.setHost(hostname);
			properties.setPort(port);
			properties.setForwardingAutoStartMode(true);
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
			con.connect(hostname, port, new HostKeyVerificationBridge());
			this.username = username;
		} catch (IOException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	public void setTimeout(int timeout) throws IOException {
		this.timeout = timeout;
		if(con != null) {
			con.setSocketTimeout(timeout);
		}
	}

	public int getTimeout() throws IOException {
		return timeout;
	}

	public boolean authenticate(SshAuthenticator[] authenticators) throws net.sf.sshapi.SshException {
		Map authenticatorMap = createAuthenticatorMap(authenticators);
		try {
			String[] methods = (String[]) con.getAvailableAuthMethods(username).toArray(new String[0]);

			for (int i = 0; i < methods.length && !con.isAuthenticated(); i++) {

				// Now at least one authentication method has been tried (none),
				// we
				// can get the banner

				SshAuthenticator authenticator = (SshAuthenticator) authenticatorMap.get(methods[i]);
				if (authenticator != null) {
					int result = con.authenticate(createAuthentication(authenticator, methods[i]));
					switch (result) {
					case AuthenticationProtocolState.COMPLETE:
						if (i == 0) {
							checkBanner();
						}
						break;
					case AuthenticationProtocolState.FAILED:
						throw new net.sf.sshapi.SshException("Authentication failed.");
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
		} catch (IOException ioe) {
			throw new net.sf.sshapi.SshException("I/O error.", ioe);
		}

		if (con.isAuthenticated()) {
			forwarding = con.getForwardingClient();
			if (getConfiguration().getX11Host() != null) {
				XDisplay xd = new XDisplay(getConfiguration().getX11Host(), getConfiguration().getX11Port() - 6000);
				forwarding.enableX11Forwarding(xd);
			}
			return true;
		}
		return false;
	}

	private String checkBanner() throws IOException {
		String banner = con.getAuthenticationBanner(2000);
		if (banner != null && !banner.trim().equals("") && getConfiguration().getBannerHandler() != null) {
			getConfiguration().getBannerHandler().banner(banner);
		}
		return banner;
	}

	private SshAuthenticationClient createAuthentication(final SshAuthenticator authenticator, String type)
			throws AuthenticationProtocolException {
		if (authenticator instanceof SshPasswordAuthenticator) {
			final SshPasswordAuthenticator passwordAuthenticator = (SshPasswordAuthenticator) authenticator;
			PasswordAuthenticationClient pac = new PasswordAuthenticationClient();
			pac.setUsername(username);
			pac.setAuthenticationPrompt(new SshAuthenticationPrompt() {

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
							AuthenticationProtocolException ape = new AuthenticationProtocolException(e.getLocalizedMessage());
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

	public SshShell createShell(String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight, byte[] terminalModes)
			throws net.sf.sshapi.SshException {
		try {
			SessionChannelClient session = con.openSessionChannel();
			if (termType != null) {
				if (terminalModes == null || terminalModes.length == 0) {
					terminalModes = new byte[] { 0 };
				}
				String modes = new String(terminalModes, "US-ASCII");
				if (!session.requestPseudoTerminal(termType, colWidth, rowHeight, pixWidth, pixHeight, modes)) {
					throw new net.sf.sshapi.SshException("Failed to allocate pseudo tty.");
				}
			}
			return new J2SshShell(getConfiguration(), session);
		} catch (net.sf.sshapi.SshException e) {
			throw e;
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
		}
	}

	public SshExtendedStreamChannel createCommand(final String command) throws net.sf.sshapi.SshException {
		try { 
			final SessionChannelClient session = con.openSessionChannel();
			return new J2SshStreamChannel(getConfiguration(), session) {
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

	public void disconnect() throws net.sf.sshapi.SshException {
		if (!isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.NOT_OPEN, "Not connected.");
		}
		con.disconnect();
	}

	public boolean isConnected() {
		return con != null && con.isConnected();
	}

	public boolean isAuthenticated() {
		return isConnected() && con.isAuthenticated();
	}

	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return con.getServerId();
	}

	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// J2SSH only supports SSH2
		return SshConfiguration.SSH2_ONLY;
	}

	public SshPortForward createLocalForward(String localAddress, int localPort, String remoteHost, int remotePort)
			throws net.sf.sshapi.SshException {

		final ForwardingConfiguration fwd = new ForwardingConfiguration("FWD" + (fwdName++), localAddress, localPort, remoteHost,
			remotePort);
		return new AbstractPortForward() {

			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					forwarding.addLocalForwarding(fwd);
					forwarding.startLocalForwarding(fwd.getName());
				} catch (ForwardingConfigurationException e) {
					throw new net.sf.sshapi.SshException("Failed to start local port forward.", e);
				}
			}

			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.stopLocalForwarding(fwd.getName());
					forwarding.removeLocalForwarding(fwd.getName());
				} catch (ForwardingConfigurationException e) {
					throw new net.sf.sshapi.SshException("Failed to stop local port forward.", e);
				}
			}
		};
	}

	public SshPortForward createRemoteForward(String remoteHost, int remotePort, String localAddress, int localPort)
			throws net.sf.sshapi.SshException {

		final ForwardingConfiguration fwd = new ForwardingConfiguration("FWD" + (fwdName++), remoteHost, remotePort, localAddress,
			localPort);
		return new AbstractPortForward() {

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

			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.stopRemoteForwarding(fwd.getName());
					forwarding.removeRemoteForwarding(fwd.getName());
				} catch (IOException e) {
					throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR,
						"Failed to stop remote port forward.", e);
				}
			}
		};
	}

	public SftpClient createSftpClient() throws net.sf.sshapi.SshException {
		return new J2SshSftpClient(con);
	}

	public SshSCPClient createSCPClient() throws net.sf.sshapi.SshException {
		return new J2SSHSCPClient();
	}

	public String getUsername() {
		return username;
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

	class HostKeyVerificationBridge implements HostKeyVerification {
		public boolean verifyHost(final String host, final SshPublicKey pk) {
			SshConfiguration configuration = getConfiguration();
			if (configuration.getHostKeyValidator() != null) {
				int status;
				try {
					status = configuration.getHostKeyValidator().verifyHost(new AbstractHostKey() {
						public String getType() {
							return pk.getAlgorithmName();
						}

						public byte[] getKey() {
							return pk.getEncoded();
						}

						public String getHost() {
							return host;
						}

						public String getFingerprint() {
							return pk.getFingerprint();
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

	public int getChannelCount() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected.");
		}
		return con.getActiveChannelCount();
	}

	class J2SSHSCPClient extends AbstractSCPClient {

		private ScpClient client;

		protected void onOpen() throws net.sf.sshapi.SshException {
			try {
				client = con.openScpClient();
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}
			super.onOpen();
		}

		public void get(String remoteFilePath, File destinationFile, boolean recursive) throws net.sf.sshapi.SshException {
			try {
				client.get(destinationFile.getAbsolutePath(), remoteFilePath, recursive);
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}
		}

		public void doPut(String remotePath, String mode, File sourceFile, boolean recursive) throws net.sf.sshapi.SshException {
			try {
				client.put(sourceFile.getAbsolutePath(), remotePath, recursive);
			} catch (IOException e) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
			}

		}

	}

	public SocketFactory createTunneledSocketFactory() throws net.sf.sshapi.SshException {
		return new RemoteSocketFactory();
	}

	class RemoteSocketFactory extends SocketFactory {

		public Socket createSocket() throws IOException {
			return new RemoteSocket();
		}

		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return new RemoteSocket(host, port);
		}

		public Socket createSocket(InetAddress host, int port) throws IOException {
			return new RemoteSocket(host, port);
		}

		public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
				UnknownHostException {
			return new RemoteSocket(host, port);
		}

		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
			return new RemoteSocket(address, port);
		}

	}

	class RemoteSocket extends AbstractSocket {

		private IOChannel channel;

		RemoteSocket() {
			super();
		}

		RemoteSocket(String host, int port) throws UnknownHostException, IOException {
			super(host, port);
		}

		RemoteSocket(InetAddress host, int port) throws UnknownHostException, IOException {
			super(host, port);
		}

		public void onConnect(InetSocketAddress addr, int timeout) throws IOException {
			if (!J2SshClient.this.isConnected()) {
				throw new IOException("SSH client is not connected");
			}
			if (!con.openChannel(channel = new RemoteSocketChannel(addr))) {
				throw new IOException("Failed to open direct-tcpip channel.");
			}
		}

		public void bind(SocketAddress bindpoint) throws IOException {
			throw new UnsupportedOperationException();
		}

		public synchronized void doClose() throws IOException {
			if (channel != null) {
				try {
					channel.close();
				} finally {
					channel = null;
				}
			}
		}

		public InputStream getInputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return channel.getInputStream();
		}

		public OutputStream getOutputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return channel.getOutputStream();
		}

		public boolean isConnected() {
			return channel != null && !isClosed();
		}
	}

	class RemoteSocketChannel extends IOChannel {

		private InetSocketAddress socket;

		RemoteSocketChannel(InetSocketAddress socket) {
			this.socket = socket;
		}

		public byte[] getChannelConfirmationData() {
			return null;
		}

		public byte[] getChannelOpenData() {
			try {
				ByteArrayWriter baw = new ByteArrayWriter();
				String host = socket.getHostName();
				baw.writeString(host);
				baw.writeInt(socket.getPort());
				baw.writeString("127.0.0.1");
				baw.writeInt(0);
				return baw.toByteArray();
			} catch (IOException ioe) {
				return null;
			}
		}

		public String getChannelType() {
			return "direct-tcpip";
		}

		protected int getMinimumWindowSpace() {
			return 32768;
		}

		protected int getMaximumWindowSpace() {
			return 131072;
		}

		protected int getMaximumPacketSize() {
			return 32768;
		}

		protected void onChannelOpen() throws IOException {
		}

		protected void onChannelRequest(String arg0, boolean arg1, byte[] arg2) throws IOException {
			connection.sendChannelRequestFailure(this);
		}

	}
}
