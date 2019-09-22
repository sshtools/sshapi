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
package net.sf.sshapi.impl.maverick16;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.net.SocketFactory;

import com.maverick.agent.client.Ssh2AgentAuthentication;
import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.PseudoTerminalModes;
import com.maverick.ssh.PublicKeyAuthentication;
import com.maverick.ssh.SocketTimeoutSupport;
import com.maverick.ssh.SshAuthentication;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshTransport;
import com.maverick.ssh.SshTunnel;
import com.maverick.ssh2.BannerDisplay;
import com.maverick.ssh2.ChannelFactory;
import com.maverick.ssh2.KBIAuthentication;
import com.maverick.ssh2.KBIRequestHandler;
import com.maverick.ssh2.Ssh2Channel;
import com.maverick.ssh2.Ssh2Client;
import com.maverick.ssh2.Ssh2Context;
import com.maverick.ssh2.Ssh2Session;
import com.sshtools.common.knownhosts.HostKeyVerification;
import com.sshtools.common.publickey.InvalidPassphraseException;
import com.sshtools.common.publickey.SshPrivateKeyFile;
import com.sshtools.common.publickey.SshPrivateKeyFileFactory;
import com.sshtools.common.ssh.ChannelOpenException;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.SshKeyPair;
import com.sshtools.common.ssh.components.SshPublicKey;
import com.sshtools.common.ssh2.KBIPrompt;
import com.sshtools.net.ForwardingClient;
import com.sshtools.net.ForwardingClientListener;
import com.sshtools.net.HttpProxyTransport;
import com.sshtools.net.SocketTransport;
import com.sshtools.net.SocketWrapper;
import com.sshtools.net.SocksProxyTransport;

import net.sf.sshapi.AbstractClient;
import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshChannel.ChannelData;
import net.sf.sshapi.SshChannelHandler;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshLifecycleListener;
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
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.hostkeys.AbstractHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.Util;

class MaverickSshClient extends AbstractClient implements ForwardingClientListener {
	private final SshConnector con;
	SshClient client;
	private ForwardingClient forwarding;
	private Map<SshTunnel, TunnelChannel> forwardingChannels = new HashMap<>();
	private SshTransport transport;
	private int timeout;
	private SshTransport connectingTransport;

	MaverickSshClient(SshConnector con, SshConfiguration configuration) throws SshException {
		super(configuration);

		this.con = con;

		HostKeyVerificationBridge hkv = new HostKeyVerificationBridge();

		// SSH2 configuration
		configureSSH2();

		con.setKnownHosts(hkv);
	}

	private void configureSSH2() throws SshException {
		Ssh2Context context2 = con.getContext();
		SshConfiguration configuration = getConfiguration();
		if (configuration.getPreferredClientToServerCipher() != null) {
			context2.setPreferredCipherCS(configuration.getPreferredClientToServerCipher());
		}
		if (configuration.getPreferredServerToClientCipher() != null) {
			context2.setPreferredCipherSC(configuration.getPreferredServerToClientCipher());
		}
		if (configuration.getPreferredClientToServerMAC() != null) {
			context2.setPreferredMacCS(configuration.getPreferredClientToServerMAC());
		}
		if (configuration.getPreferredServerToClientMAC() != null) {
			context2.setPreferredMacSC(configuration.getPreferredServerToClientMAC());
		}
		if (configuration.getPreferredClientToServerCompression() != null) {
			context2.setPreferredCompressionCS(configuration.getPreferredClientToServerCompression());
		}
		if (configuration.getPreferredServerToClientCompression() != null) {
			context2.setPreferredCompressionCS(configuration.getPreferredServerToClientCompression());
		}
		if (configuration.getPreferredKeyExchange() != null) {
			context2.setPreferredKeyExchange(configuration.getPreferredKeyExchange());
		}
		if (configuration.getPreferredPublicKey() != null) {
			context2.setPreferredPublicKey(configuration.getPreferredPublicKey());
		}

		context2.setBannerDisplay(new BannerDisplayBridge());
	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		this.timeout = timeout;
		if (transport != null && transport instanceof Socket) {
			((Socket) transport).setSoTimeout(timeout);
		} else if (transport != null && transport instanceof SocketTimeoutSupport) {
			((SocketTimeoutSupport) transport).setSoTimeout(timeout);
		}
	}

	@Override
	public int getTimeout() throws IOException {
		if (transport != null && transport instanceof Socket) {
			return ((Socket) transport).getSoTimeout();
		} else if (transport != null && transport instanceof SocketTimeoutSupport) {
			return ((SocketTimeoutSupport) transport).getSoTimeout();
		}
		return timeout;
	}

	@Override
	public void addChannelHandler(final SshChannelHandler channelFactory) throws net.sf.sshapi.SshException {
		if (client == null) {
			// TODO does this really need to be true?
			throw new IllegalStateException("Must be connected to add a channel handler.");
		} else if (client instanceof Ssh2Client) {
			try {
				MaverickChannelFactoryAdapter factory = new MaverickChannelFactoryAdapter(channelFactory);
				((Ssh2Client) client).addChannelFactory(factory);
			} catch (SshException e) {
				throw new net.sf.sshapi.SshException(e);
			}
		} else {
			throw new UnsupportedOperationException("Channel factories are not possible with SSH1");
		}
	}

	class MaverickSshChannel extends
			AbstractDataProducingComponent<SshChannelListener<net.sf.sshapi.SshChannel>, net.sf.sshapi.SshChannel>
			implements net.sf.sshapi.SshChannel {

		private Ssh2Channel ssh2Channel;
		private ChannelData channelData;
		private String name;

		public MaverickSshChannel(String name, ChannelData channelData) {
			this.channelData = channelData;
			this.name = name;
		}

		/**
		 * Inform all listeners the channel has reached EOF.
		 */
		protected void fireEof() {
			if (getListeners() != null) {
				for (int i = getListeners().size() - 1; i >= 0; i--)
					getListeners().get(i).eof(this);
			}
		}

		/**
		 * Inform all listeners a request was received.
		 * 
		 * @param requestType request type
		 * @param wantReply   remote side wanted reply
		 * @param data        data
		 * @return send error reply
		 */
		protected boolean fireRequest(String requestType, boolean wantReply, byte[] data) {
			boolean send = false;
			if (getListeners() != null) {
				for (int i = getListeners().size() - 1; i >= 0; i--) {
					if (getListeners().get(i).request(this, requestType, wantReply, data)) {
						send = true;
					}
				}
			}
			return send;
		}

		public void setSourceChannel(Ssh2Channel ssh2Channel) {
			this.ssh2Channel = ssh2Channel;
			ssh2Channel.addChannelEventListener(new ChannelEventListener() {
				@Override
				public void extendedDataReceived(SshChannel arg0, byte[] buf, int off, int len, int arg4) {
					fireData(SshDataListener.EXTENDED, buf, off, len);
				}

				@Override
				public void dataSent(SshChannel arg0, byte[] buf, int off, int len) {
					fireData(SshDataListener.SENT, buf, off, len);
				}

				@Override
				public void dataReceived(SshChannel arg0, byte[] buf, int off, int len) {
					fireData(SshDataListener.RECEIVED, buf, off, len);
				}

				@Override
				public void channelOpened(SshChannel arg0) {
					fireOpened();
				}

				@Override
				public void channelEOF(SshChannel arg0) {
					fireEof();
				}

				@Override
				public void channelClosing(SshChannel arg0) {
					fireClosing();
				}

				@Override
				public void channelClosed(SshChannel arg0) {
					fireClosed();
				}
			});
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return ssh2Channel.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return ssh2Channel.getOutputStream();
		}

		@Override
		public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata)
				throws net.sf.sshapi.SshException {
			try {
				return ssh2Channel.sendRequest(requesttype, wantreply, requestdata);
			} catch (SshException e) {
				throw new net.sf.sshapi.SshException("Failed to send request.", e);
			}
		}

		@Override
		protected void onOpen() throws net.sf.sshapi.SshException {
		}

		@Override
		protected void onClose() throws net.sf.sshapi.SshException {
			ssh2Channel.close();
		}

		@Override
		public ChannelData getChannelData() {
			return channelData;
		}

		@Override
		public String getName() {
			return name;
		}

	}

	class MaverickChannelFactoryAdapter implements ChannelFactory {

		private SshChannelHandler sshApiFactory;

		MaverickChannelFactoryAdapter(SshChannelHandler sshApiFactory) {
			this.sshApiFactory = sshApiFactory;
		}

		@Override
		public Ssh2Channel createChannel(final String name, byte[] requestData) {
			final ChannelData channelData = sshApiFactory.createChannel(name, requestData);
			final MaverickSshChannel msc = new MaverickSshChannel(name, channelData);
			Ssh2Channel ssh2Channel = new Ssh2Channel(name, channelData.getWindowSize(), channelData.getPacketSize()) {
				@Override
				protected byte[] create() {
					return channelData.create();
				}

				@Override
				protected void channelRequest(String requesttype, boolean wantreply, byte[] requestdata)
						throws SshException {
					super.channelRequest(requesttype, msc.fireRequest(requesttype, wantreply, requestdata),
							requestdata);
				}
			};
			msc.setSourceChannel(ssh2Channel);
			try {
				sshApiFactory.channelCreated(msc);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
			return ssh2Channel;
		}

		@Override
		public String[] supportedChannelTypes() {
			return sshApiFactory.getSupportChannelNames();
		}

	}

	@Override
	protected void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws net.sf.sshapi.SshException {

		try {
			transport = null;
			SshProxyServerDetails proxyServer = getConfiguration().getProxyServer();

			// TODO can Maverick have a socket factory set when using a Proxy?
			SocketFactory socketFactory = getConfiguration().getSocketFactory();
			if (socketFactory != null) {
				transport = new SocketWrapper(socketFactory.createSocket(hostname, port));
			} else {
				if (proxyServer != null) {
					if (proxyServer.getType().equals(SshProxyServerDetails.Type.HTTP)) {
						transport = HttpProxyTransport.connectViaProxy(hostname, port, proxyServer.getHostname(),
								proxyServer.getPort(), proxyServer.getUsername(), new String(proxyServer.getPassword()),
								getConfiguration().getProperties()
										.getProperty(MaverickSshProvider.CFG_HTTP_PROXY_USER_AGENT, "SSHAPI/Maverick"));
					} else if (proxyServer.getType().equals(SshProxyServerDetails.Type.SOCKS4)) {
						transport = SocksProxyTransport.connectViaSocks4Proxy(hostname, port, proxyServer.getHostname(),
								proxyServer.getPort(), proxyServer.getUsername());
					} else if (proxyServer.getType().equals(SshProxyServerDetails.Type.SOCKS5)) {
						transport = SocksProxyTransport.connectViaSocks5Proxy(hostname, port, proxyServer.getHostname(),
								proxyServer.getPort(),
								"true".equals(getConfiguration().getProperties()
										.getProperty(MaverickSshProvider.CFG_SOCKS5_PROXY_LOCAL_LOOKUP, "false")),
								proxyServer.getUsername(), new String(proxyServer.getPassword()));
					}
				}
			}
			if (transport == null) {
				transport = new SocketTransport(hostname, port);
			}
			if (timeout > 0) {
				setTimeout(timeout);
			}
			connectingTransport = transport;
			try {
				client = con.connect(transport, username, true, null);
			} finally {
				connectingTransport = null;
			}
		} catch (IOException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		} catch (SshException e) {
			if (e.getMessage().startsWith("The host key was not accepted")) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.HOST_KEY_REJECTED, e);
			} else {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
			}
		}
	}

	@Override
	public boolean isConnected() {
		return client != null && client.isConnected();
	}

	@Override
	public boolean isAuthenticated() {
		return isConnected() && client.isAuthenticated();
	}

	private String[] getAuthenticationMethods() throws SshException {
		String[] authenticationMethods = ((Ssh2Client) client).getAuthenticationMethods(client.getUsername());
		if (getProvider().getCapabilities().contains(Capability.AGENT)) {
			String[] a = new String[authenticationMethods.length + 1];
			System.arraycopy(authenticationMethods, 0, a, 1, authenticationMethods.length);
			a[0] = "agent";
			authenticationMethods = a;
		}
		return authenticationMethods;
	}

	@Override
	public int getChannelCount() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected.");
		}
		return client.getChannelCount();
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws net.sf.sshapi.SshException {
		Map<String, SshAuthenticator> authenticatorMap = createAuthenticatorMap(authenticators);
		try {
			String[] methods = getAuthenticationMethods();

			for (int i = 0; i < methods.length && !client.isAuthenticated(); i++) {
				SshAuthenticator authenticator = authenticatorMap.get(methods[i]);
				if (authenticator != null) {
					int result = client.authenticate(createAuthentication(authenticator, methods[i]));
					switch (result) {
					case SshAuthentication.COMPLETE:
						break;
					case SshAuthentication.FAILED:
						// throw new
						// net.sf.sshapi.SshException("Authentication failed.");
					case SshAuthentication.FURTHER_AUTHENTICATION_REQUIRED:
						continue;
					case SshAuthentication.CANCELLED:
						throw new net.sf.sshapi.SshException("Authentication cancelled.");
					case SshAuthentication.PUBLIC_KEY_ACCEPTABLE:
						break;
					default:
						throw new net.sf.sshapi.SshException("Unknown authentication result " + result + ".");
					}
				}
			}
		} catch (SshException sshe) {
			throw new net.sf.sshapi.SshException("Failed to authenticate.", sshe);
		}

		if (client.isAuthenticated()) {
			forwarding = new ForwardingClient(client);
			forwarding.addListener(this);
			SshConfiguration configuration = getConfiguration();
			if (configuration.getX11Host() != null) {
				try {
					forwarding.allowX11Forwarding(
							configuration.getX11Host() + ":" + (configuration.getX11Port() - 6000),
							Util.formatAsHexString(configuration.getX11Cookie()));
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to configure X11 forwarding.", e);
				}
			}
			return true;
		}
		return false;
	}

	private SshAuthentication createAuthentication(final SshAuthenticator authenticator, String type)
			throws net.sf.sshapi.SshException {
		if (authenticator instanceof SshAgentAuthenticator) {
			SshAgentAuthenticator aa = (SshAgentAuthenticator) authenticator;
			return new Ssh2AgentAuthentication(((MaverickAgent) aa.getAgent()).getAgent());
		} else if (authenticator instanceof SshPasswordAuthenticator) {
			return new PasswordAuthentication() {
				@Override
				public String getPassword() {
					char[] answer = ((SshPasswordAuthenticator) authenticator).promptForPassword(MaverickSshClient.this,
							"Password");
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
						char[] pa = pk.promptForPassphrase(MaverickSshClient.this, "Passphrase");
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
				PublicKeyAuthentication pka = new PublicKeyAuthentication();
				pka.setUsername(client.getUsername());
				pka.setPrivateKey(pair.getPrivateKey());
				pka.setPublicKey(pair.getPublicKey());
				return pka;
			} catch (IOException ioe) {
				throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, ioe);
			}
		}

		// NOT PORTED YET - Remember to add back in capability when it is

		// else if (authenticator instanceof SshGSSAPIAuthenticator) {
		// final SshGSSAPIAuthenticator gssa = (SshGSSAPIAuthenticator) authenticator;
		// Ssh2GSSAPIAuthenticationWithMIC gss = new
		// Ssh2GSSAPIAuthenticationWithMIC(gssa.getConfiguration());
		// gss.setCallbackHandler(new CallbackHandler() {
		//
		// public void handle(final Callback[] callbacks) throws IOException,
		// UnsupportedCallbackException {
		// final Callback cl = callbacks[0];
		// if (cl instanceof NameCallback) {
		// ((NameCallback) cl).setName(client.getUsername());
		// }
		// if (cl instanceof PasswordCallback) {
		// final char[] promptForPassword =
		// gssa.promptForPassword(MaverickSshClient.this,
		// ((PasswordCallback) cl).getPrompt());
		// ((PasswordCallback) cl).setPassword(promptForPassword);
		// }
		//
		// }
		//
		// });
		// gss.setUsername(client.getUsername());
		// return gss;
		// }
		else if (authenticator instanceof SshKeyboardInteractiveAuthenticator) {
			final SshKeyboardInteractiveAuthenticator kbi = (SshKeyboardInteractiveAuthenticator) authenticator;
			KBIAuthentication kbia = new KBIAuthentication();
			kbia.setKBIRequestHandler(new KBIRequestHandler() {

				@Override
				public boolean showPrompts(String name, String instruction, KBIPrompt[] prompts) {
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
						return true;
					}
					return false;
				}
			});
			kbia.setUsername(client.getUsername());
			return kbia;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public SshShell createShell(String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight,
			byte[] terminalModes) throws net.sf.sshapi.SshException {
		try {
			SshConfiguration.getLogger().log(Level.DEBUG, "Opening session channel");
			com.maverick.ssh.SshSession session = client.openSessionChannel();
			if (termType != null) {
				SshConfiguration.getLogger().log(Level.DEBUG, "Requesting pty for " + termType + " " + colWidth + "x"
						+ rowHeight + " [" + pixWidth + "x" + pixHeight + "] = " + terminalModes);
				requestPty(client, termType, colWidth, rowHeight, pixWidth, pixHeight, terminalModes, session);
			}
			return new MaverickSshShell(session);
		} catch (net.sf.sshapi.SshException sshe) {
			throw sshe;
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
		}
	}

	private void requestPty(SshClient client, String termType, int colWidth, int rowHeight, int pixWidth, int pixHeight,
			byte[] terminalModes, com.maverick.ssh.SshSession session) throws net.sf.sshapi.SshException {
		try {
			PseudoTerminalModes ptm = new PseudoTerminalModes(client);
			if (terminalModes != null) {
				for (int i = 0; i < terminalModes.length; i++) {
					ptm.setTerminalMode(terminalModes[i], true);
				}
			}
			if (!session.requestPseudoTerminal(termType, colWidth, rowHeight, pixWidth, pixHeight, ptm)) {
				throw new net.sf.sshapi.SshException("Failed to allocate pseudo tty.");
			}
		} catch (SshException e) {
			throw new net.sf.sshapi.SshException(e);
		}
	}

	@Override
	public SshCommand createCommand(final String command) throws net.sf.sshapi.SshException {
		try {
			final com.maverick.ssh.SshSession session = client.openSessionChannel();
			return new MaverickSshStreamChannel(session) {

				@Override
				public void onChannelOpen() throws net.sf.sshapi.SshException {
					try {
						session.executeCommand(command);
					} catch (Exception e) {
						throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
					}
				}
			};
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException("Failed to open session channel.", e);
		}
	}

	@Override
	public String getUsername() {
		return client.getUsername();
	}

	@Override
	public SshPortForward createLocalForward(final String localAddress, final int localPort, final String remoteHost,
			final int remotePort) throws net.sf.sshapi.SshException {

		final String fLocalAddress = localAddress == null ? "0.0.0.0" : localAddress;
		return new AbstractPortForward() {

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					forwarding.startLocalForwarding(fLocalAddress, localPort, remoteHost, remotePort);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to start local forward.", e);
				}
			}

			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.stopLocalForwarding(fLocalAddress + ":" + localPort, true);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to stop local forward.", e);
				}
			}
		};
	}

	@Override
	public SshPortForward createRemoteForward(final String remoteHost, final int remotePort, final String localAddress,
			final int localPort) throws net.sf.sshapi.SshException {

		final String fRemoteHost = remoteHost == null ? "0.0.0.0" : remoteHost;
		return new AbstractPortForward() {

			@Override
			protected void onOpen() throws net.sf.sshapi.SshException {
				try {
					forwarding.requestRemoteForwarding(fRemoteHost, remotePort, localAddress, localPort);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to start remote forward.", e);
				}
			}

			@Override
			protected void onClose() throws net.sf.sshapi.SshException {
				try {
					forwarding.cancelRemoteForwarding(fRemoteHost, remotePort);
				} catch (SshException e) {
					throw new net.sf.sshapi.SshException("Failed to stop remote forward.", e);
				}
			}
		};
	}

	@Override
	public void disconnect() throws net.sf.sshapi.SshException {
		if (!isConnected() && connectingTransport == null) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.NOT_OPEN, "Not connected.");
		}
		try {
			if (connectingTransport != null) {
				try {
					connectingTransport.close();
				} catch (IOException e) {
				}
			}
			if (client != null) {
				client.disconnect();
			}
		} finally {
			if (forwarding != null) {
				forwarding.removeListener(this);
			}
		}
	}

	@Override
	public SftpClient createSftp() throws net.sf.sshapi.SshException {
		return new MaverickSftpClient(this, client);
	}

	@Override
	public SshSCPClient createSCP() throws net.sf.sshapi.SshException {
		return new MaverickSCPClient(this);
	}

	@Override
	public String getRemoteIdentification() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return client.getRemoteIdentification();
	}

	@Override
	public int getRemoteProtocolVersion() {
		if (!isConnected()) {
			throw new IllegalStateException("Not connected");
		}
		return client.getVersion();
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws net.sf.sshapi.SshException {
		return new RemoteSocketFactory(client);
	}

	class RemoteSocketFactory extends SocketFactory {

		private SshClient client;

		public RemoteSocketFactory(SshClient client) {
			this.client = client;
		}

		@Override
		public Socket createSocket() throws IOException {
			return new RemoteSocket(client);
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			return new RemoteSocket(client, host, port);
		}

		@Override
		public Socket createSocket(InetAddress host, int port) throws IOException {
			return new RemoteSocket(client, host.getHostAddress(), port);
		}

		@Override
		public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
				throws IOException, UnknownHostException {
			return new RemoteSocket(client, host, port);
		}

		@Override
		public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
				throws IOException {
			return new RemoteSocket(client, address.getHostAddress(), port);
		}

	}

	class RemoteSocket extends AbstractSocket implements ChannelEventListener {

		private SshTunnel channel;
		private SshClient client;

		RemoteSocket(SshClient client) {
			super();
			this.client = client;
		}

		RemoteSocket(SshClient client, String host, int port) throws UnknownHostException, IOException {
			super();
			this.client = client;
			this.connect(new InetSocketAddress(host, port));
		}

		@Override
		public void onConnect(InetSocketAddress addr, int timeout) throws IOException {
			if (client == null || !client.isConnected()) {
				throw new IOException("Not connected.");
			}
			try {
				channel = client.openForwardingChannel(addr.getHostName(), addr.getPort(), null, 0, "127.0.0.1", 0,
						null, this);
			} catch (Exception e) {
				IOException ioe = new IOException("Failed to open direct-tcpip channel.");
				ioe.initCause(e);
				throw ioe;
			}
		}

		@Override
		public void bind(SocketAddress bindpoint) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public synchronized void doClose() throws IOException {
			if (channel != null) {
				try {
					channel.close();
				} finally {
					channel = null;
				}
			}
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return channel.getInputStream();
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (!isConnected()) {
				throw new IOException("Not connected.");
			}
			return channel.getOutputStream();
		}

		@Override
		public boolean isConnected() {
			return channel != null && !isClosed();
		}

		@Override
		public void channelClosed(SshChannel channel) {
		}

		@Override
		public void channelClosing(SshChannel channel) {
		}

		@Override
		public void channelEOF(SshChannel channel) {
		}

		@Override
		public void channelOpened(SshChannel channel) {
		}

		@Override
		public void dataReceived(SshChannel channel, byte[] data, int off, int len) {
		}

		@Override
		public void dataSent(SshChannel channel, byte[] data, int off, int len) {
		}

		@Override
		public void extendedDataReceived(SshChannel channel, byte[] data, int off, int len, int extendedDataType) {
		}
	}

	class HostKeyVerificationBridge implements HostKeyVerification {
		@Override
		public boolean verifyHost(final String host, final SshPublicKey pk) throws SshException {
			if (getConfiguration().getHostKeyValidator() != null) {
				int status;
				try {
					status = getConfiguration().getHostKeyValidator().verifyHost(new AbstractHostKey() {

						@Override
						public String getType() {
							return pk.getAlgorithm();
						}

						@Override
						public byte[] getKey() {
							try {
								return pk.getEncoded();
							} catch (SshException e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public String getHost() {
							return host;
						}

						@Override
						public String getFingerprint() {
							try {
								return pk.getFingerprint();
							} catch (SshException e) {
								throw new RuntimeException(e);
							}
						}

						@Override
						public String getComments() {
							return null;
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
		@Override
		public void displayBanner(String message) {
			getConfiguration().getBannerHandler().banner(message);
		}
	}

	@Override
	public void channelClosed(int type, String key, SshTunnel tunnel) {
		int sshapiType = getTypeForTunnel(type);
		TunnelChannel channel = forwardingChannels.get(tunnel);
		if (channel != null) {
			try {
				firePortForwardChannelClosed(sshapiType, channel);
			} finally {
				forwardingChannels.remove(tunnel);
			}
		} else {
			SshConfiguration.getLogger().log(Level.WARN,
					"Got a channel closed event for a channel we don't know about (" + key + ").");
		}
	}

	@Override
	public void channelFailure(int type, String key, String host, int port, boolean isConnected, Throwable t) {
		// TODO?
	}

	@Override
	public void channelOpened(int type, String key, SshTunnel tunnel) {
		int sshapiType = getTypeForTunnel(type);
		TunnelChannel channel = new TunnelChannel(tunnel);
		forwardingChannels.put(tunnel, channel);
		// The channel is actually already open, but this will set its state
		// correctly in the wrapper
		try {
			channel.open();
		} catch (net.sf.sshapi.SshException e) {
			throw new RuntimeException(e);
		}
		firePortForwardChannelOpened(sshapiType, channel);
	}

	private int getTypeForTunnel(int type) {
		int sshapiType = SshPortForward.LOCAL_FORWARDING;
		if (type == ForwardingClientListener.X11_FORWARDING) {
			sshapiType = SshPortForward.X11_FORWARDING;
		} else if (type == ForwardingClientListener.REMOTE_FORWARDING) {
			sshapiType = SshPortForward.REMOTE_FORWARDING;
		}
		return sshapiType;
	}

	@Override
	public SshPublicKeySubsystem createPublicKeySubsystem() throws net.sf.sshapi.SshException {
		try {
			final com.maverick.ssh.SshSession session = client.openSessionChannel();
			if (session instanceof Ssh2Session) {
				return new MaverickPublicKeySubsystem((Ssh2Session) session);
			} else {
				session.close();
			}
			throw new UnsupportedOperationException();
		} catch (SshException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		} catch (ChannelOpenException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}

	}

	@Override
	public void forwardingStarted(int type, String key, String host, int port) {
	}

	@Override
	public void forwardingStopped(int type, String key, String host, int port) {
	}

	class TunnelChannel
			extends AbstractDataProducingComponent<SshLifecycleListener<SshPortForwardTunnel>, SshPortForwardTunnel>
			implements SshPortForwardTunnel {

		private SshTunnel tunnel;

		public TunnelChannel(SshTunnel tunnel) {
			this.tunnel = tunnel;
			tunnel.addChannelEventListener(new ChannelEventListener() {

				@Override
				public void extendedDataReceived(SshChannel channel, byte[] data, int off, int len,
						int extendedDataType) {
				}

				@Override
				public void dataReceived(SshChannel channel, byte[] data, int off, int len) {
					fireData(SshDataListener.RECEIVED, data, off, len);
				}

				@Override
				public void dataSent(SshChannel channel, byte[] data, int off, int len) {
					fireData(SshDataListener.SENT, data, off, len);
				}

				@Override
				public void channelOpened(SshChannel channel) {
				}

				@Override
				public void channelEOF(SshChannel channel) {
				}

				@Override
				public void channelClosing(SshChannel channel) {
				}

				@Override
				public void channelClosed(SshChannel channel) {
				}
			});
		}

		@Override
		public String getBindAddress() {
			return tunnel.getListeningAddress();
		}

		@Override
		public int getBindPort() {
			return tunnel.getListeningPort();
		}

		@Override
		public String getOriginatingAddress() {
			return tunnel.getOriginatingHost();
		}

		@Override
		public int getOriginatingPort() {
			return tunnel.getOriginatingPort();
		}

		@Override
		public String toString() {
			return "TunnelChannel [getBindAddress()=" + getBindAddress() + ", getBindPort()=" + getBindPort()
					+ ", getOriginatingAddress()=" + getOriginatingAddress() + ", getOriginatingPort()="
					+ getOriginatingPort() + "]";
		}

		@Override
		protected void onClose() throws net.sf.sshapi.SshException {
		}

		@Override
		protected void onOpen() throws net.sf.sshapi.SshException {
		}

	}

	@Override
	public boolean checkLocalSourceAddress(SocketAddress arg0, String arg1, int arg2, String arg3, int arg4) {
		return true;
	}
}
