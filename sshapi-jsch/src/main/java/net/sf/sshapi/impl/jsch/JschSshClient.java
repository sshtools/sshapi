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
package net.sf.sshapi.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import com.jcraft.jsch.ChannelDirectTCPIP;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.HASH;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractForwardingChannel;
import net.sf.sshapi.DefaultSCPClient;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
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

class JschSshClient extends AbstractClient implements Logger {
	protected final static class ForwardingChannel extends AbstractForwardingChannel<JschSshClient> {
		private ChannelDirectTCPIP localForward;

		protected ForwardingChannel(JschSshClient client, SshProvider provider, SshConfiguration configuration,
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
			try {
				localForward = (ChannelDirectTCPIP) client.session.openChannel("direct-tcpip");
				localForward.setHost(hostname);
				localForward.setPort(port);
				localForward.connect();
			} catch (JSchException e) {
				throw new SshException("Failed to open direct-tcpip channel.", e);
			}
		}

		@Override
		protected void onCloseStream() throws net.sf.sshapi.SshException {
			localForward.disconnect();
		}
	}

	class HostKeyRepositoryBridge implements HostKeyRepository {
		HostKeyRepository knownHosts;

		public HostKeyRepositoryBridge(HostKeyRepository knownHosts) {
			this.knownHosts = knownHosts;
		}

		@Override
		public void add(HostKey hostkey, UserInfo ui) {
			knownHosts.add(hostkey, ui);
		}

		@Override
		public int check(final String host, final byte[] key) {
			SshHostKeyValidator hostKeyValidator = getConfiguration().getHostKeyValidator();
			if (hostKeyValidator != null) {
				try {
					Class<?> c = Class.forName(JSch.getConfig("md5"));
					final HASH hash = (HASH) (c.newInstance());
					switch (hostKeyValidator.verifyHost(new AbstractHostKey() {
						@Override
						public String getFingerprint() {
							return getFingerPrint(hash, key);
						}

						@Override
						public String getHost() {
							return host;
						}

						@Override
						public byte[] getKey() {
							return key;
						}

						@Override
						public String getType() {
							switch (key[8]) {
							case 'd':
								return SshConfiguration.PUBLIC_KEY_SSHDSA;
							case 'r':
								return SshConfiguration.PUBLIC_KEY_SSHRSA;
							default:
								throw new UnsupportedOperationException();
							}
						}

					})) {
					case SshHostKeyValidator.STATUS_HOST_CHANGED:
						return HostKeyRepository.CHANGED;
					case SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN:
						return HostKeyRepository.NOT_INCLUDED;
					case SshHostKeyValidator.STATUS_HOST_KEY_VALID:
						return HostKeyRepository.OK;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return knownHosts.check(host, key);
		}

		@Override
		public HostKey[] getHostKey() {
			return knownHosts.getHostKey();
		}

		@Override
		public HostKey[] getHostKey(String host, String type) {
			return knownHosts.getHostKey(host, type);
		}

		@Override
		public String getKnownHostsRepositoryID() {
			return knownHosts.getKnownHostsRepositoryID();
		}

		@Override
		public void remove(String host, String type) {
			knownHosts.remove(host, type);
		}

		@Override
		public void remove(String host, String type, byte[] key) {
			knownHosts.remove(host, type, key);
		}
	}

	class UserInfoAuthenticatorBridge implements UserInfo, UIKeyboardInteractive {
		private final SshKeyboardInteractiveAuthenticator keyboardInteractiveAuthenticator;
		private char[] passphrase;
		private char[] password;
		private final SshPasswordAuthenticator passwordAuthenticator;
		private final SshPublicKeyAuthenticator publicKeyAuthenticator;

		public UserInfoAuthenticatorBridge(SshPasswordAuthenticator passwordAuthenticator,
				SshPublicKeyAuthenticator publicKeyAuthenticator,
				SshKeyboardInteractiveAuthenticator keyboardInteractiveAuthenticator, SshHostKeyValidator hkv) {
			this.passwordAuthenticator = passwordAuthenticator;
			this.publicKeyAuthenticator = publicKeyAuthenticator;
			this.keyboardInteractiveAuthenticator = keyboardInteractiveAuthenticator;
		}

		@Override
		public String getPassphrase() {
			return passphrase == null ? null : new String(passphrase);
		}

		@Override
		public String getPassword() {
			return password == null ? null : new String(password);
		}

		@Override
		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
				boolean[] echo) {
			if (keyboardInteractiveAuthenticator != null) {
				return keyboardInteractiveAuthenticator.challenge(name, instruction, prompt, echo);
			}
			return null;
		}

		@Override
		public boolean promptPassphrase(String message) {
			passphrase = publicKeyAuthenticator == null ? null
					: publicKeyAuthenticator.promptForPassphrase(JschSshClient.this, message);
			return passphrase != null;
		}

		@Override
		public boolean promptPassword(String message) {
			password = passwordAuthenticator == null ? null
					: passwordAuthenticator.promptForPassword(JschSshClient.this, message);
			return password != null;
		}

		@Override
		public boolean promptYesNo(String message) {
			return false;
		}

		@Override
		public void showMessage(String message) {
			if (getConfiguration().getBannerHandler() != null) {
				getConfiguration().getBannerHandler().banner(message);
			}
		}
	}

	private static String[] chars = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;
		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

	// Copied from JSch source as it is not public
	static String getFingerPrint(HASH hash, byte[] data) {
		try {
			hash.init();
			hash.update(data, 0, data.length);
			byte[] foo = hash.digest();
			StringBuffer sb = new StringBuffer();
			int bar;
			for (int i = 0; i < foo.length; i++) {
				bar = foo[i] & 0xff;
				sb.append(chars[(bar >>> 4) & 0xf]);
				sb.append(chars[(bar) & 0xf]);
				if (i + 1 < foo.length)
					sb.append(":");
			}
			return sb.toString();
		} catch (Exception e) {
			return "???";
		}
	}

	private boolean authenticated;
	private int channelCount;
	// Private instance variables
	private JSch client;
	private boolean pseudoConnected;
	private Session session;
	private Socket socket;
	private int timeout = 0;

	public JschSshClient(SshConfiguration configuration) {
		super(configuration);
		client = new JSch();
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
		SshPasswordAuthenticator paw = (SshPasswordAuthenticator) authenticatorMap.get("password");
		SshPublicKeyAuthenticator pk = (SshPublicKeyAuthenticator) authenticatorMap.get("publickey");
		List<String> auths = new ArrayList<>();
		SshKeyboardInteractiveAuthenticator ki = (SshKeyboardInteractiveAuthenticator) authenticatorMap
				.get("keyboard-interactive");
		if (pk != null) {
			try {
				auths.add("publickey");
				client.addIdentity(pk.getPrivateKeyFile().getAbsolutePath());
			} catch (JSchException e) {
				throw new SshException(SshException.GENERAL, e);
			}
		}
		if (paw != null)
			auths.add("password");
		if (ki != null)
			auths.add("keyboard-interactive");
		session.setConfig("PreferredAuthentications", String.join(",", auths));
		session.setUserInfo(new UserInfoAuthenticatorBridge(paw, pk, ki, getConfiguration().getHostKeyValidator()));
		return attemptAuth();
	}

	@Override
	protected SshChannel doCreateForwardingChannel(String hostname, int port) throws SshException {
		return new ForwardingChannel(this, getProvider(), getConfiguration(), hostname, port);
	}

	@Override
	public int getChannelCount() {
		return channelCount;
	}

	public JSch getJSchClient() {
		return client;
	}

	@Override
	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// This is required because there is no separate connect / authenticate
		// phase with JSch
		if (session.isConnected()) {
			return session.getServerVersion();
		} else {
			return "Unknown";
		}
	}

	@Override
	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// Jsch only supports SSH2
		return SshConfiguration.SSH2_ONLY;
	}

	@Override
	public int getTimeout() throws IOException {
		return session == null ? timeout : session.getTimeout();
	}

	@Override
	public String getUsername() {
		return session.getUserName();
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && authenticated;
	}

	@Override
	public boolean isConnected() {
		return pseudoConnected || session != null;
	}

	@Override
	public boolean isEnabled(int level) {
		net.sf.sshapi.Logger logger = SshConfiguration.getLogger();
		switch (level) {
		case Logger.DEBUG:
			return logger.isDebug();
		case Logger.ERROR:
			return logger.isError();
		case Logger.INFO:
			return logger.isInfo();
		case Logger.WARN:
			return logger.isWarn();
		default:
			return logger.isError();
		}
	}

	@Override
	public void log(int level, String message) {
		net.sf.sshapi.Logger logger = SshConfiguration.getLogger();
		Level sshapiLevel = Level.ERROR;
		switch (level) {
		case Logger.DEBUG:
			sshapiLevel = Level.DEBUG;
			break;
		case Logger.INFO:
			sshapiLevel = Level.INFO;
			break;
		case Logger.WARN:
			sshapiLevel = Level.WARN;
			break;
		default:
			sshapiLevel = Level.ERROR;
			break;
		}
		logger.log(sshapiLevel, message);
	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		try {
			this.timeout = timeout;
			if (session != null)
				session.setTimeout(timeout);
		} catch (JSchException e) {
			IOException ioe = new IOException("Failed to set timeout.");
			ioe.initCause(e);
			throw ioe;
		}
	}

	@Override
	protected void checkConnectedAndAuthenticated() throws SshException {
		if (pseudoConnected) {
			if (!attemptAuth())
				throw new SshException(SshException.NOT_AUTHENTICATED);
		}
		super.checkConnectedAndAuthenticated();
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		try {
			JSch.setLogger(this);
			client.setHostKeyRepository(new HostKeyRepositoryBridge(client.getHostKeyRepository()));
			session = client.getSession(username, hostname, port);
			session.setConfig("MaxAuthTries", "1");
			if (timeout > -1)
				session.setTimeout(timeout);
			com.jcraft.jsch.SocketFactory sfactory = null;
			final SocketFactory socketFactory = getConfiguration().getSocketFactory();
			if (socketFactory != null) {
				sfactory = new com.jcraft.jsch.SocketFactory() {
					@Override
					public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
						return socketFactory.createSocket();
					}

					@Override
					public InputStream getInputStream(Socket socket) throws IOException {
						return socket.getInputStream();
					}

					@Override
					public OutputStream getOutputStream(Socket socket) throws IOException {
						return socket.getOutputStream();
					}
				};
			}
			/*
			 * Because jSch doesnt separate the connect / authenticate phases, we try to
			 * work around this by creating the socket now so we can get an error. We then
			 * use this socket as the first one returned in a custom socket factory.
			 */
			socket = null;
			interruptable();
			try {
				if (sfactory == null) {
					socket = new Socket(hostname, port);
					if(timeout > -1)
						socket.setSoTimeout(timeout);
					Socket fsocket = socket;
					session.setSocketFactory(new com.jcraft.jsch.SocketFactory() {
						Socket ssocket = fsocket;

						@Override
						public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
							if (ssocket != null) {
								try {
									return ssocket;
								} finally {
									ssocket = null;
								}
							} else
								return new Socket(host, port);
						}

						@Override
						public InputStream getInputStream(Socket socket) throws IOException {
							return socket.getInputStream();
						}

						@Override
						public OutputStream getOutputStream(Socket socket) throws IOException {
							return socket.getOutputStream();
						}
					});
				} else {
					socket = sfactory.createSocket(hostname, port);
					Socket fsocket = socket;
					com.jcraft.jsch.SocketFactory ffactory = sfactory;
					session.setSocketFactory(new com.jcraft.jsch.SocketFactory() {
						Socket ssocket = fsocket;

						@Override
						public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
							if (ssocket != null) {
								try {
									return ssocket;
								} finally {
									ssocket = null;
								}
							} else
								return ffactory.createSocket(host, port);
						}

						@Override
						public InputStream getInputStream(Socket socket) throws IOException {
							return socket.getInputStream();
						}

						@Override
						public OutputStream getOutputStream(Socket socket) throws IOException {
							return socket.getOutputStream();
						}
					});
				}
				SshConfiguration configuration = getConfiguration();
				if (configuration.getX11Host() != null) {
					session.setX11Host(configuration.getX11Host());
				}
				if (configuration.getX11Screen() > -1) {
					session.setX11Port(configuration.getX11Screen() + 6000);
				}
				if (configuration.getX11Cookie() != null) {
					String hexString = Util.formatAsHexString(configuration.getX11Cookie());
					session.setX11Cookie(hexString);
				}
				SshProxyServerDetails proxyServer = configuration.getProxyServer();
				if (proxyServer != null) {
					if (proxyServer.getType().equals(SshProxyServerDetails.Type.HTTP)) {
						ProxyHTTP proxy = new ProxyHTTP(proxyServer.getHostname(), proxyServer.getPort());
						proxy.setUserPasswd(proxyServer.getUsername(), new String(proxyServer.getPassword()));
						session.setProxy(proxy);
					} else if (proxyServer.getType().equals(SshProxyServerDetails.Type.SOCKS4)) {
						ProxySOCKS4 proxy = new ProxySOCKS4(proxyServer.getHostname(), proxyServer.getPort());
						proxy.setUserPasswd(proxyServer.getUsername(), new String(proxyServer.getPassword()));
						session.setProxy(proxy);
					} else if (proxyServer.getType().equals(SshProxyServerDetails.Type.SOCKS5)) {
						ProxySOCKS5 proxy = new ProxySOCKS5(proxyServer.getHostname(), proxyServer.getPort());
						proxy.setUserPasswd(proxyServer.getUsername(), new String(proxyServer.getPassword()));
						session.setProxy(proxy);
					}
				}
			} finally {
				uninterruptable();
			}
		} catch (JSchException e) {
			throw new SshException(SshException.GENERAL, e);
		} catch (UnknownHostException e) {
			throw new SshException(SshException.GENERAL, e);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	protected SshCommand doCreateCommand(String command, String termType, int colWidth, int rowHeight, int pixWidth,
			int pixHeight, byte[] terminalModes) throws SshException {
		try {
			ChannelExec channel = (ChannelExec) session.openChannel("exec");
			if (termType != null) {
				channel.setTerminalMode(
						terminalModes == null || terminalModes.length == 0 ? new byte[0] : terminalModes);
				channel.setPtyType(termType, colWidth, rowHeight, pixWidth, pixHeight);
				channel.setPty(true);
			}
			channel.setCommand(command);
			return new JschStreamChannel(getProvider(), getConfiguration(), channel) {
				@Override
				protected void onChannelClose() throws SshException {
					channelCount--;
				}

				@Override
				protected void onChannelOpen() throws SshException {
					channelCount++;
				}
			};
		} catch (JSchException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	@Override
	protected SshPortForward doCreateLocalForward(final String localAddress, final int localPort,
			final String remoteHost, final int remotePort) throws SshException {
		return new AbstractPortForward(getProvider()) {
			private int boundPort;

			@Override
			public int getBoundPort() {
				return boundPort;
			}

			@Override
			protected void onClose() throws SshException {
				try {
					session.delPortForwardingL(localAddress, boundPort);
				} catch (JSchException e) {
					throw new SshException("Failed to close local port forward", e);
				} finally {
					channelCount--;
					boundPort = 0;
				}
			}

			@Override
			protected void onOpen() throws SshException {
				try {
					if (localPort == 0)
						boundPort = session.setPortForwardingL(localAddress, Util.findRandomPort(), remoteHost,
								remotePort);
					else
						boundPort = session.setPortForwardingL(localAddress, localPort, remoteHost, remotePort);
					if (boundPort < 1 && localPort > 0)
						boundPort = localPort;

					channelCount++;
				} catch (JSchException e) {
					throw new SshException("Failed to configure local port forward");
				}
			}
		};
	}

	@Override
	protected SshPortForward doCreateRemoteForward(final String remoteHost, final int remotePort,
			final String localAddress, final int localPort) throws SshException {
		return new AbstractPortForward(getProvider()) {

			@Override
			protected void onClose() throws SshException {
				try {
					session.delPortForwardingR(remotePort);
				} catch (JSchException e) {
					throw new SshException("Failed to configure remote port forward");
				} finally {
					channelCount--;
				}
			}

			@Override
			protected void onOpen() throws SshException {
				try {
					session.setPortForwardingR(remoteHost, remotePort, localAddress, localPort);
					channelCount++;
				} catch (JSchException e) {
					throw new SshException("Failed to configure remote port forward");
				}
			}
		};
	}

	@Override
	protected SshSCPClient doCreateSCP() throws SshException {
		return new DefaultSCPClient(this) {

			@Override
			protected void onClose() throws SshException {
				super.onClose();
				channelCount--;
			}

			@Override
			protected void onOpen() throws SshException {
				super.onOpen();
				channelCount++;
			}
		};
	}

	@Override
	protected SftpClient doCreateSftp() throws SshException {
		try {
			ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
			return new JschSftpClient(getProvider(), channel, getConfiguration()) {
				@Override
				public void onClose() throws SshException {
					super.onClose();
					channelCount--;
				}

				@Override
				public void onOpen() throws SshException {
					super.onOpen();
					channelCount++;
				}
			};
		} catch (JSchException e) {
			throw new SshException("Failed to open SFTP channel.", e);
		}
	}

	@Override
	protected SshShell doCreateShell(String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		try {
			ChannelShell channel = (ChannelShell) session.openChannel("shell");
			if (termType != null) {
				channel.setTerminalMode(
						terminalModes == null || terminalModes.length == 0 ? new byte[0] : terminalModes);
				channel.setPtyType(termType, colWidth, rowHeight, pixWidth, pixHeight);
				channel.setPty(true);
			} else
				channel.setPty(false);
			return new JschSshShell(getProvider(), getConfiguration(), channel) {
				@Override
				protected void onChannelClose() throws SshException {
					channelCount--;
				}

				@Override
				protected void onShellOpen() throws SshException {
					channelCount++;
				}
			};
		} catch (JSchException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	@Override
	protected void onClose() throws SshException {
		if (!isConnected()) {
			throw new SshException(SshException.NOT_OPEN, "Not connected.");
		}
		authenticated = false;
		try {
			if (pseudoConnected)
				pseudoConnected = false;
			else
				session.disconnect();
		} finally {
			session = null;
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ioe) {
				}
			}
		}
	}

	private boolean attemptAuth() throws SshException {
		SshConfiguration configuration = getConfiguration();
		try {
			interruptable();
			session.connect(Integer.parseInt(
					configuration.getProperties().getProperty(JschSshProvider.CFG_SESSION_CONNECT_TIMEOUT, "30000")));
			authenticated = true;
			return true;
		} catch (NumberFormatException e) {
			throw new RuntimeException(e);
		} catch (JSchException e) {
			if (e.getMessage().equals("Auth cancel")) {
				throw new SshException(SshException.AUTHENTICATION_CANCELLED, e);
			}
			if (e.getMessage().equals("Auth fail")) {
				pseudoConnected = true;
				return false;
			} else
				throw new SshException("Failed to authenticate.", e);
		} finally {
			uninterruptable();
		}
	}
}
