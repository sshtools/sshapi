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
package net.sf.sshapi.impl.ganymed;

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

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.HTTPProxyData;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.KnownHosts;
import ch.ethz.ssh2.LocalPortForwarder;
import ch.ethz.ssh2.LocalStreamForwarder;
import ch.ethz.ssh2.ServerHostKeyVerifier;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.auth.AuthenticationManager;
import ch.ethz.ssh2.channel.ChannelManager;
import ch.ethz.ssh2.crypto.CryptoWishList;
import ch.ethz.ssh2.crypto.PEMDecoder;
import ch.ethz.ssh2.crypto.PEMDecryptException;
import ch.ethz.ssh2.crypto.cipher.BlockCipherFactory;
import ch.ethz.ssh2.crypto.digest.MAC;
import ch.ethz.ssh2.transport.KexManager;
import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
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

class GanymedSshClient extends AbstractClient {
	class RemoteSocket extends AbstractSocket {
		private Connection connection;
		private LocalStreamForwarder streamForwarder;

		RemoteSocket(Connection connection) {
			super();
			this.connection = connection;
		}

		RemoteSocket(Connection connection, InetAddress host, int port) throws UnknownHostException, IOException {
			super();
			this.connection = connection;
			this.connect(new InetSocketAddress(host, port));
		}

		RemoteSocket(Connection connection, String host, int port) throws UnknownHostException, IOException {
			super();
			this.connection = connection;
			this.connect(new InetSocketAddress(InetAddress.getByName(host), port));
		}

		@Override
		public void bind(SocketAddress bindpoint) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void doClose() throws IOException {
			if (streamForwarder != null) {
				try {
					streamForwarder.close();
				} finally {
					streamForwarder = null;
				}
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return streamForwarder.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return streamForwarder.getOutputStream();
		}

		@Override
		public boolean isConnected() {
			return streamForwarder != null && !isClosed();
		}

		@Override
		public void onConnect(InetSocketAddress addr, int timeout) throws IOException {
			if (connection == null) {
				throw new IOException("Not connected.");
			}
			streamForwarder = connection.createLocalStreamForwarder(addr.getHostName(), addr.getPort());
		}
	}

	class RemoteSocketFactory extends SocketFactory {
		@Override
		public Socket createSocket() throws IOException {
			return new RemoteSocket(connection);
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return new RemoteSocket(connection, host, port);
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
			return new RemoteSocket(connection, address, port);
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return new RemoteSocket(connection, host, port);
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			return new RemoteSocket(connection, host, port);
		}
	}

	class ServerHostKeyVerifierBridge implements ServerHostKeyVerifier {
		private SshHostKeyValidator hostKeyValidator;

		public ServerHostKeyVerifierBridge(SshHostKeyValidator hostKeyValidator) {
			this.hostKeyValidator = hostKeyValidator;
		}

		@Override
		public boolean verifyServerHostKey(final String hostname, int port, final String serverHostKeyAlgorithm,
				final byte[] serverHostKey) throws Exception {
			final String hexFingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
			if (hostKeyValidator != null) {
				switch (hostKeyValidator.verifyHost(new AbstractHostKey() {
					@Override
					public String getComments() {
						return null;
					}

					@Override
					public String getFingerprint() {
						return hexFingerprint;
					}

					@Override
					public String getHost() {
						return hostname;
					}

					@Override
					public byte[] getKey() {
						return serverHostKey;
					}

					@Override
					public String getType() {
						return serverHostKeyAlgorithm;
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

	protected static byte[] checkTerminalModes(byte[] terminalModes) {
		if (terminalModes != null && terminalModes.length > 0 && terminalModes[terminalModes.length - 1] != 0) {
			byte[] b = new byte[terminalModes.length + 1];
			System.arraycopy(terminalModes, 0, b, 0, terminalModes.length);
			terminalModes = b;
		}
		return terminalModes;
	}

	// Private instance variables
	Connection connection;
	private boolean connected;
	private final SecureRandom rng;

	public GanymedSshClient(SshConfiguration configuration, SecureRandom rng) {
		super(configuration);
		this.rng = rng;
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
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

	@Override
	protected SshPortForward doCreateLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws SshException {
		if (localAddress != null && !localAddress.equals("0.0.0.0")) {
			throw new IllegalArgumentException("Ganymed does not supporting binding a local port forward to a particular address.");
		}
		return new AbstractPortForward(getProvider()) {
			private LocalPortForwarder localPortForwarder;

			@Override
			public int getBoundPort() {
				return localPortForwarder.getLocalSocketAddress().getPort();
			}

			@Override
			protected void onClose() throws SshException {
				try {
					localPortForwarder.close();
				} catch (IOException e) {
					throw new SshException("Failed to stop local port forward.", e);
				}
			}

			@Override
			protected void onOpen() throws SshException {
				try {
					localPortForwarder = connection.createLocalPortForwarder(localPort, remoteHost, remotePort);
				} catch (IOException e) {
					throw new SshException("Failed to open local port forward.", e);
				}
			}
		};
	}

	@Override
	protected SshPortForward doCreateRemoteForward(final String remoteHost, final int remotePort, final String localAddress,
			final int localPort) throws SshException {
		return new AbstractPortForward(getProvider()) {

			@Override
			protected void onClose() throws SshException {
				try {
					connection.cancelRemotePortForwarding(remotePort);
				} catch (IOException e) {
					throw new SshException("Failed to stop remote port forward.", e);
				}
			}

			@Override
			protected void onOpen() throws SshException {
				try {
					connection.requestRemotePortForwarding(remoteHost, remotePort, localAddress, localPort);
				} catch (IOException e) {
					throw new SshException("Failed to open remote port forward.", e);
				}
			}
		};
	}

	@Override
	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		try {
			Session sess = connection.openSession();
			checkTerminalModes(terminalModes);
			if (termType != null) {
				sess.requestPTY(termType, cols, rows, 0, 0, terminalModes);
			}
			return new GanymedSshShell(getProvider(), getConfiguration(), sess);
		} catch (IOException e) {
			throw new SshException("Failed to create shell channel.", e);
		}
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws SshException {
		return new RemoteSocketFactory();
	}

	@Override
	public int getChannelCount() {
		try {
			Field cmF = connection.getClass().getDeclaredField("cm");
			cmF.setAccessible(true);
			ChannelManager cm = (ChannelManager) cmF.get(connection);
			Field cF = cm.getClass().getDeclaredField("channels");
			cF.setAccessible(true);
			Vector<?> channels = (Vector<?>) cF.get(cm);
			return channels == null ? 0 : channels.size();
		} catch (Exception e) {
			throw new UnsupportedOperationException("Could not determine number of channels open.", e);
		}
	}

	@Override
	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return "Unknown";
	}

	@Override
	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		// Ganymed only supports SSH2
		return SshConfiguration.SSH2_ONLY;
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && connection.isAuthenticationComplete();
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators) throws SshException {
		SshConfiguration configuration = getConfiguration();
		if (configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			throw new SshException(SshException.UNSUPPORTED_PROTOCOL_VERSION,
					"Ganymed only supports SSH2, yet SSH1 only was request.");
		}
		SshProxyServerDetails proxyServer = configuration.getProxyServer();
		if (proxyServer != null) {
			connection = new Connection(hostname, port, new HTTPProxyData(proxyServer.getHostname(), proxyServer.getPort(),
					proxyServer.getUsername(), new String(proxyServer.getPassword())));
		} else {
			connection = new Connection(hostname, port);
		}
		connection.setSecureRandom(rng);
		configureAlgorithms(configuration);
		try {
			connection.connect(new ServerHostKeyVerifierBridge(configuration.getHostKeyValidator()));
			connected = true;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", e);
		}
	}

	@Override
	protected SshCommand doCreateCommand(final String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		try {
			final Session sess = connection.openSession();
			checkTerminalModes(terminalModes);
			if (termType != null) {
				sess.requestPTY(termType, cols, rows, 0, 0, terminalModes);
			}
			return new GanymedStreamChannel(getProvider(), getConfiguration(), sess) {
				@Override
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

	@Override
	protected SshSCPClient doCreateSCP() throws SshException {
		return new GanymedSCPClient(this);
	}

	@Override
	protected SftpClient doCreateSftp() throws SshException {
		return new GanymedSftpClient(this, connection);
	}

	@Override
	protected void onClose() throws SshException {
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

	private String[] checkCipher(String cipher) {
		List<String> ciphers = new ArrayList<>(Arrays.asList(BlockCipherFactory.getDefaultCipherList()));
		ciphers.remove(cipher);
		ciphers.add(0, cipher);
		return ciphers.toArray(new String[ciphers.size()]);
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

	private String[] checkMAC(String mac) {
		List<String> macs = new ArrayList<>(Arrays.asList(MAC.getMacList()));
		macs.remove(mac);
		macs.add(0, mac);
		return macs.toArray(new String[macs.size()]);
	}

	private String[] checkPublicKey(String publicKey) {
		List<String> pks = new ArrayList<>(Arrays.asList(KexManager.getDefaultServerHostkeyAlgorithmList()));
		pks.remove(publicKey);
		pks.add(0, publicKey);
		return pks.toArray(new String[pks.size()]);
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
				List<String> l = new ArrayList<>(Arrays.asList(cwl.kexAlgorithms));
				l.remove(preferredKeyExchange);
				l.add(0, preferredKeyExchange);
				cwl.kexAlgorithms = l.toArray(new String[l.size()]);
			} catch (Exception e) {
				SshConfiguration.getLogger().log(Level.ERROR, "Could not set key exchange.", e);
			}
		}
		String preferredPublicKey = configuration.getPreferredPublicKey();
		if (preferredPublicKey != null) {
			connection.setServerHostKeyAlgorithms(checkPublicKey(preferredPublicKey));
		}
	}

	private boolean doAuthentication(SshAuthenticator[] authenticators) throws IOException, SshException {
		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
		while (true) {
			String[] remainingMethods = connection.getRemainingAuthMethods(getUsername());
			if (remainingMethods == null || remainingMethods.length == 0) {
				throw new SshException(SshException.AUTHENTICATION_FAILED, "No remaining authentication methods");
			}
			for (int i = 0; i < remainingMethods.length; i++) {
				SshAuthenticator authenticator = authenticatorMap.get(remainingMethods[i]);
				if (authenticator != null) {
					// Password
					if (authenticator instanceof SshPasswordAuthenticator) {
						char[] pw = ((SshPasswordAuthenticator) authenticator).promptForPassword(this, "Password");
						if (pw == null) {
							throw new SshException("Authentication cancelled.");
						}
						if (connection.authenticateWithPassword(getUsername(), new String(pw))) {
							// Authenticated!
							return true;
						}
						// Return to main loop so getRemainingMethods is called
						// again
						continue;
					}
					// Public key
					if (authenticator instanceof SshPublicKeyAuthenticator) {
						SshPublicKeyAuthenticator pk = (SshPublicKeyAuthenticator) authenticator;
						char[] charArray = new String(pk.getPrivateKey(), "US-ASCII").toCharArray();
						char[] pw = null;
						// Try to work out if key is encrypted
						try {
							PEMDecoder.decode(charArray, null);
						} catch (IOException ioe) {
							// Encrypted (probably)
							pw = pk.promptForPassphrase(this, "Passphrase");
							if (pw == null) {
								throw new SshException("Authentication cancelled.");
							}
						}
						try {
							if (connection.authenticateWithPublicKey(getUsername(), charArray,
									pw == null ? null : new String(pw))) {
								// Authenticated!
								return true;
							}
						} catch (IOException ioe) {
							if (ioe.getCause() instanceof PEMDecryptException)
								return false;
							else
								throw ioe;
						}
						// Return to main loop so getRemainingMethods is called
						// again
						continue;
					}
					// Keyboard interactive
					if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
						final SshKeyboardInteractiveAuthenticator kbi = (SshKeyboardInteractiveAuthenticator) authenticator;
						if (connection.authenticateWithKeyboardInteractive(getUsername(), new InteractiveCallback() {
							@Override
							public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
									boolean[] echo) throws Exception {
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
}
