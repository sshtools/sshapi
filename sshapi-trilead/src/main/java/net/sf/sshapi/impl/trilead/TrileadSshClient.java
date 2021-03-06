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
package net.sf.sshapi.impl.trilead;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.net.SocketFactory;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.LocalPortForwarder;
import com.trilead.ssh2.LocalStreamForwarder;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.auth.AuthenticationManager;
import com.trilead.ssh2.channel.ChannelManager;
import com.trilead.ssh2.crypto.CryptoWishList;
import com.trilead.ssh2.crypto.PEMDecoder;
import com.trilead.ssh2.crypto.cipher.BlockCipherFactory;
import com.trilead.ssh2.crypto.digest.MAC;
import com.trilead.ssh2.transport.KexManager;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedStreamChannel;
import net.sf.sshapi.SshProxyServerDetails;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAgentAuthenticator;
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

class TrileadSshClient extends AbstractClient {
	// Private instance variables
	Connection connection;
	private String username;
	private boolean connected;
	private final SecureRandom rng;

	public TrileadSshClient(SshConfiguration configuration, SecureRandom rng) {
		super(configuration);
		this.rng = rng;
	}

	public void connect(String username, String hostname, int port) throws SshException {
		if (isConnected()) {
			throw new SshException(SshException.ALREADY_OPEN, "Already connected.");
		}
		SshConfiguration configuration = getConfiguration();
		if (configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			throw new SshException(SshException.UNSUPPORTED_PROTOCOL_VERSION,
					"Trilead only supports SSH2, yet SSH1 only was request.");
		}

		SshProxyServerDetails proxyServer = configuration.getProxyServer();
		connection = new Connection(hostname, port);
		if (proxyServer != null) {
			connection.setProxyData(new HTTPProxyData(proxyServer.getHostname(), proxyServer.getPort(),
					proxyServer.getUsername(), new String(proxyServer.getPassword())));
		}
		connection.setSecureRandom(rng);
		configureAlgorithms(configuration);

		try {
			connection.connect(new ServerHostKeyVerifierBridge(configuration.getHostKeyValidator()));
			connected = true;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", e);
		}
		this.username = username;
	}

	public SocketFactory createTunneledSocketFactory() throws SshException {
		return new RemoteSocketFactory();
	}

	public SshSCPClient createSCPClient() throws SshException {
		return new TriliadSCPClient(this);
	}

	public boolean authenticate(SshAuthenticator[] authenticators) throws SshException {
		try {
			if (!doAuthentication(authenticators)) {
				return false;
			}
			checkForBanner();
			return true;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	private boolean doAuthentication(SshAuthenticator[] authenticators) throws IOException, SshException {
		Map authenticatorMap = createAuthenticatorMap(authenticators);
		while (true) {
			String[] remainingMethods = connection.getRemainingAuthMethods(username);
			if (remainingMethods == null || remainingMethods.length == 0) {
				throw new SshException(SshException.AUTHENTICATION_FAILED, "No remaining authentication methods");
			}
			for (int i = 0; i < remainingMethods.length; i++) {
				SshAuthenticator authenticator = (SshAuthenticator) authenticatorMap.get(remainingMethods[i]);
				if (authenticator != null) {

					// Password
					if (authenticator instanceof SshPasswordAuthenticator) {
						char[] pw = ((SshPasswordAuthenticator) authenticator).promptForPassword(this, "Password");
						if (pw == null) {
							throw new SshException("Authentication cancelled.");
						}
						if (connection.authenticateWithPassword(username, new String(pw))) {
							// Authenticated!
							return true;
						}

						// Return to main loop so getRemainingMethods is called
						// again
						continue;
					}

					// Public key
					if (authenticator instanceof SshPublicKeyAuthenticator) {
						SshPublicKeyAuthenticator pka = ((SshPublicKeyAuthenticator) authenticator);
						byte[] keyBytes = pka.getPrivateKey();
						char[] pw = null;
						char[] charArray = new String(keyBytes, "US-ASCII").toCharArray();

						// Try to work out if key is encrypted
						try {
							PEMDecoder.decode(charArray, null);
						} catch (IOException ioe) {
							// Encrypted (probably)
							pw = pka.promptForPassphrase(this, "Passphrase");
							if (pw == null) {
								throw new SshException("Authentication cancelled.");
							}
						}

						if (connection.authenticateWithPublicKey(username, charArray,
								pw == null ? null : new String(pw))) {
							// Authenticated!
							return true;
						}

						// Return to main loop so getRemainingMethods is called
						// again
						continue;
					}

					// Keyboard interactive
					if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
						final SshKeyboardInteractiveAuthenticator kbi = (SshKeyboardInteractiveAuthenticator) authenticator;
						if (connection.authenticateWithKeyboardInteractive(username, new InteractiveCallback() {
							public String[] replyToChallenge(String name, String instruction, int numPrompts,
									String[] prompt, boolean[] echo) throws Exception {
								return kbi.challenge(name, instruction, prompt, echo);
							}
						})) {
							// Authenticated!
							return true;
						}

						continue;
					}
				}
			}

			return false;
		}
	}

	private void checkForBanner() {
		try {
			Field amF = connection.getClass().getDeclaredField("am");
			amF.setAccessible(true);
			AuthenticationManager am = (AuthenticationManager) amF.get(connection);
			Field bannerF = am.getClass().getDeclaredField("banner");
			bannerF.setAccessible(true);
			String banner = (String) bannerF.get(am);
			if (banner != null && !banner.equals("") && getConfiguration().getBannerHandler() != null) {
				getConfiguration().getBannerHandler().banner(banner);
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Failed to access banner", e);
		}
	}

	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return "Unknown";
	}

	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// Trilead only supports SSH2
		return SshConfiguration.SSH2_ONLY;
	}

	public SshPortForward createLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws SshException {
		if (localAddress != null && !localAddress.equals("0.0.0.0")) {
			throw new IllegalArgumentException(
					"Trilead does not supporting binding a local port forward to a particular address.");
		}
		return new AbstractPortForward() {

			private LocalPortForwarder localPortForwarder;

			protected void onOpen() throws SshException {
				try {
					localPortForwarder = connection.createLocalPortForwarder(localPort, remoteHost, remotePort);
				} catch (IOException e) {
					throw new SshException("Failed to open local port forward.", e);
				}
			}

			protected void onClose() throws SshException {
				try {
					localPortForwarder.close();
				} catch (IOException e) {
					throw new SshException("Failed to stop local port forward.", e);
				}
			}
		};
	}

	public SshPortForward createRemoteForward(final String remoteHost, final int remotePort, final String localAddress,
			final int localPort) throws SshException {
		return new AbstractPortForward() {
			protected void onOpen() throws SshException {
				try {
					connection.requestRemotePortForwarding(remoteHost, remotePort, localAddress, localPort);
				} catch (IOException e) {
					throw new SshException("Failed to open remote port forward.", e);
				}
			}

			protected void onClose() throws SshException {
				try {
					connection.cancelRemotePortForwarding(remotePort);
				} catch (IOException e) {
					throw new SshException("Failed to stop remote port forward.", e);
				}
			}
		};
	}

	public SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		try {
			Session sess = connection.openSession();
			checkTerminalModes(terminalModes);
			if (termType != null) {
				sess.requestPTY(termType, cols, rows, 0, 0, null);
			}
			return new TrileadSshShell(getConfiguration(), sess);
		} catch (IOException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	public SshExtendedStreamChannel createCommand(final String command) throws SshException {
		try {
			final Session sess = connection.openSession();
			return new TrileadStreamChannel(getConfiguration(), sess) {
				public void onChannelOpen() throws SshException {
					try {
						sess.execCommand(command);
					} catch (IOException e) {
						throw new SshException(SshException.IO_ERROR, e);
					}
				}
			};
		} catch (IOException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	public void disconnect() throws SshException {
		if (!isConnected()) {
			throw new SshException(SshException.NOT_OPEN, "Not connected.");
		}
		try {
			connection.close();
		} finally {
			connected = false;
			connection = null;
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean isAuthenticated() {
		return isConnected() && connection.isAuthenticationComplete();
	}

	public SftpClient createSftpClient() throws SshException {
		return new TrileadSftpClient(connection);
	}

	public String getUsername() {
		return username;
	}

	private void configureAlgorithms(SshConfiguration configuration) {
		String preferredClientToServerCipher = configuration.getPreferredClientToServerCipher();
		if (preferredClientToServerCipher != null) {
			connection.setClient2ServerCiphers(checkCipher(preferredClientToServerCipher));
		}
		String preferredServerToClientCipher = configuration.getPreferredServerToClientCipher();
		if (preferredServerToClientCipher != null) {
			connection.setServer2ClientCiphers(checkCipher(preferredServerToClientCipher));
		}
		String preferredClientToServerMAC = configuration.getPreferredClientToServerMAC();
		if (preferredClientToServerMAC != null) {
			connection.setClient2ServerMACs(checkMAC(preferredClientToServerMAC));
		}
		String preferredServerToClientMAC = configuration.getPreferredServerToClientMAC();
		if (preferredServerToClientMAC != null) {
			connection.setServer2ClientMACs(checkMAC(preferredServerToClientMAC));
		}
		String preferredKeyExchange = configuration.getPreferredKeyExchange();
		if (preferredKeyExchange != null) {
			try {
				// Nasty reflection hack to set the preferred key exchange
				Field field = connection.getClass().getDeclaredField("cryptoWishList");
				field.setAccessible(true);
				CryptoWishList cwl = (CryptoWishList) field.get(connection);
				List l = new ArrayList(Arrays.asList(cwl.kexAlgorithms));
				l.remove(preferredKeyExchange);
				l.add(0, preferredKeyExchange);
				cwl.kexAlgorithms = (String[]) l.toArray(new String[l.size()]);
			} catch (Exception e) {
				SshConfiguration.getLogger().log(Level.ERROR, "Could not set key exchange.", e);
			}
		}
		String preferredPublicKey = configuration.getPreferredPublicKey();
		if (preferredPublicKey != null) {
			connection.setServerHostKeyAlgorithms(checkPublicKey(preferredPublicKey));
		}
	}

	private String[] checkCipher(String cipher) {
		List ciphers = new ArrayList(Arrays.asList(BlockCipherFactory.getDefaultCipherList()));
		ciphers.remove(cipher);
		ciphers.add(0, cipher);
		return (String[]) ciphers.toArray(new String[ciphers.size()]);
	}

	private String[] checkMAC(String mac) {
		List macs = new ArrayList(Arrays.asList(MAC.getMacList()));
		macs.remove(mac);
		macs.add(0, mac);
		return (String[]) macs.toArray(new String[macs.size()]);
	}

	private String[] checkPublicKey(String publicKey) {
		List pks = new ArrayList(Arrays.asList(KexManager.getDefaultServerHostkeyAlgorithmList()));
		pks.remove(publicKey);
		pks.add(0, publicKey);
		return (String[]) pks.toArray(new String[pks.size()]);
	}

	class ServerHostKeyVerifierBridge implements ServerHostKeyVerifier {
		private SshHostKeyValidator hostKeyValidator;

		public ServerHostKeyVerifierBridge(SshHostKeyValidator hostKeyValidator) {
			this.hostKeyValidator = hostKeyValidator;
		}

		public boolean verifyServerHostKey(final String hostname, int port, final String serverHostKeyAlgorithm,
				final byte[] serverHostKey) throws Exception {
			final String hexFingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
			if (hostKeyValidator != null) {
				switch (hostKeyValidator.verifyHost(new AbstractHostKey() {

					public String getType() {
						return serverHostKeyAlgorithm;
					}

					public byte[] getKey() {
						return serverHostKey;
					}

					public String getHost() {
						return hostname;
					}

					public String getFingerprint() {
						return hexFingerprint;
					}
				})) {
				case SshHostKeyValidator.STATUS_HOST_KEY_VALID:
					return true;
				}
				return false;
			} else {
				System.out.println("The authenticity of host '" + hostname + "' can't be established.");
				System.out.println(serverHostKeyAlgorithm + " key fingerprint is " + hexFingerprint);
				return Util.promptYesNo("Are you sure you want to continue connecting?");
			}
		}
	}

	public int getChannelCount() {
		try {
			Field cmF = connection.getClass().getDeclaredField("cm");
			cmF.setAccessible(true);
			ChannelManager cm = (ChannelManager) cmF.get(connection);
			Field cF = cm.getClass().getDeclaredField("channels");
			cF.setAccessible(true);
			Vector channels = (Vector) cF.get(cm);
			return channels == null ? 0 : channels.size();
		} catch (Exception e) {
			throw new UnsupportedOperationException("Could not determine number of channels open.", e);
		}
	}

	protected static byte[] checkTerminalModes(byte[] terminalModes) {
		if (terminalModes != null && terminalModes.length > 0 && terminalModes[terminalModes.length - 1] != 0) {
			byte[] b = new byte[terminalModes.length + 1];
			System.arraycopy(terminalModes, 0, b, 0, terminalModes.length);
			terminalModes = b;
		}
		return terminalModes;
	}

	class RemoteSocketFactory extends SocketFactory {

		public Socket createSocket() throws IOException {
			return new RemoteSocket(connection);
		}

		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return new RemoteSocket(connection, host, port);
		}

		public Socket createSocket(InetAddress host, int port) throws IOException {
			return new RemoteSocket(connection, host, port);
		}

		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			return new RemoteSocket(connection, host, port);
		}

		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			return new RemoteSocket(connection, address, port);
		}

	}

	class RemoteSocket extends AbstractSocket {

		private LocalStreamForwarder streamForwarder;
		private Connection connection;

		RemoteSocket(Connection connection) {
			super();
			this.connection = connection;
		}

		RemoteSocket(Connection connection, String host, int port) throws UnknownHostException, IOException {
			super();
			this.connection = connection;
			this.connect(new InetSocketAddress(InetAddress.getByName(host), port));
		}

		RemoteSocket(Connection connection, InetAddress host, int port) throws UnknownHostException, IOException {
			super();
			this.connection = connection;
			this.connect(new InetSocketAddress(host, port));
		}

		public void onConnect(InetSocketAddress addr, int timeout) throws IOException {
			if (connection == null) {
				throw new IOException("Not connected.");
			}
			streamForwarder = connection.createLocalStreamForwarder(addr.getHostName(), addr.getPort());
		}

		public void bind(SocketAddress bindpoint) throws IOException {
			throw new UnsupportedOperationException();
		}

		public synchronized void doClose() throws IOException {
			if (streamForwarder != null) {
				try {
					streamForwarder.close();
				} finally {
					streamForwarder = null;
				}
			}
		}

		public InputStream getInputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return streamForwarder.getInputStream();
		}

		public OutputStream getOutputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return streamForwarder.getOutputStream();
		}

		public boolean isConnected() {
			return streamForwarder != null && !isClosed();
		}
	}
}
