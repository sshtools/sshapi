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
package net.sf.sshapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.net.SocketFactory;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.Util;

/**
 * Abstract implementation of a {@link SshClient}. All provider client
 * implementations will probably want to extend this as it provides some basic
 * common services.
 */
public abstract class AbstractClient implements SshClient {
	protected Set<SshLifecycleComponent<?, ?>> activeComponents = Collections.synchronizedSet(new LinkedHashSet<>());
	private SshConfiguration configuration;
	private String hostname;
	private int port;
	private List<SshPortForwardListener> portForwardlisteners = new ArrayList<>();
	private SshProvider provider;
	private String username;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 */
	public AbstractClient(SshConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void addChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	@Override
	public synchronized void addPortForwardListener(SshPortForwardListener listener) {
		portForwardlisteners.add(listener);
	}

	@Override
	public Future<Boolean> authenticateLater(SshAuthenticator... authenticators) {
		AbstractFuture<Boolean> authenticateFuture = new AbstractFuture<Boolean>() {
			@Override
			Boolean doFuture() throws Exception {
				return authenticate(authenticators);
			}
		};
		getProvider().getExecutor().submit(authenticateFuture.createRunnable());
		return authenticateFuture;
	}

	@Override
	public void close() throws IOException {
		try {
			onClose();
		} catch (SshException e) {
			throw new IOException("Failed to close.", e);
		} finally {
			username = null;
			hostname = null;
			port = 0;
		}
	}

	@Override
	public void closeQuietly() {
		try {
			close();
		} catch (Exception e) {
		}
	}

	@Override
	public final SshCommand command(String command) throws SshException {
		return command(command, null, 0, 0, 0, 0, null);
	}

	@Override
	public SshCommand command(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		SshCommand sshCommand = createCommand(command, termType, cols, rows, pixWidth, pixHeight, terminalModes);
		sshCommand.open();
		return sshCommand;
	}

	@Override
	public final Future<Void> connectLater(String spec, SshAuthenticator... authenticators) {
		return connectLater(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public Future<Void> connectLater(String username, String hostname, int port, SshAuthenticator... authenticators) {
		return new AbstractFuture<Void>() {
			@Override
			Void doFuture() throws Exception {
				connect(username, hostname, port, authenticators);;
				return null;
			}
		};
	}

	@Override
	public final void connect(String spec, SshAuthenticator... authenticators) throws SshException {
		connect(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public final void connect(String username, String hostname, int port, SshAuthenticator... authenticators) throws SshException {
		if (isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.ALREADY_OPEN, "Already connected.");
		}
		this.username = username;
		this.port = port;
		this.hostname = hostname;
		doConnect(username, hostname, port);
		postConnect();
		if (authenticators.length > 0) {
			try {
				for (int i = 0; i < getConfiguration().getMaxAuthAttempts() && !authenticate(authenticators); i++)
					;
				if (!isAuthenticated())
					throw new SshException(SshException.AUTHENTICATION_FAILED);
			} catch (SshException sshe) {
				try {
					close();
				} catch (IOException e) {
				}
				throw sshe;
			} catch (RuntimeException re) {
				try {
					close();
				} catch (IOException ioe) {
				}
				throw re;
			}
		}
	}

	@Override
	public final SshCommand createCommand(String command) throws SshException {
		return createCommand(command, null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshCommand createCommand(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		checkConnectedAndAuthenticated();
		SshCommand client = doCreateCommand(command, termType, cols, rows, pixWidth, pixHeight, terminalModes);
		client.addListener(new SshChannelListener<SshCommand>() {
			@Override
			public void closed(SshCommand channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SshPortForward createLocalForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		checkConnectedAndAuthenticated();
		SshPortForward client = doCreateLocalForward(localBindAddress, localBindPort, targetAddress, targetPort);
		client.addListener(new SshPortForwardListener() {
			@Override
			public void closed(SshPortForward channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SshPublicKeySubsystem createPublicKeySubsystem() throws SshException {
		checkConnectedAndAuthenticated();
		SshPublicKeySubsystem client = doCreatePublicKeySubsystem();
		client.addListener(new SshLifecycleListener<SshPublicKeySubsystem>() {
			@Override
			public void closed(SshPublicKeySubsystem channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SshPortForward createRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		checkConnectedAndAuthenticated();
		SshPortForward client = doCreateRemoteForward(remoteBindAddress, remoteBindPort, targetAddress, targetPort);
		client.addListener(new SshPortForwardListener() {
			@Override
			public void closed(SshPortForward channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SshSCPClient createSCP() throws SshException {
		checkConnectedAndAuthenticated();
		SshSCPClient client = doCreateSCP();
		client.addListener(new SshLifecycleListener<SshSCPClient>() {
			@Override
			public void closed(SshSCPClient channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SftpClient createSftp() throws SshException {
		checkConnectedAndAuthenticated();
		SftpClient client = doCreateSftp();
		client.addListener(new SshLifecycleListener<SftpClient>() {
			@Override
			public void closed(SftpClient channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public final SshShell createShell() throws SshException {
		return createShell(null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		checkConnectedAndAuthenticated();
		SshShell client = doCreateShell(termType, cols, rows, pixWidth, pixHeight, terminalModes);
		client.addListener(new SshChannelListener<SshShell>() {
			@Override
			public void closed(SshShell channel) {
				activeComponents.remove(channel);
			}
		});
		activeComponents.add(client);
		return client;
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws SshException {
		throw new UnsupportedOperationException("Tunneled socket factory is not supported in this implementation.");
	}

	@Override
	public void forceKeyExchange() throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SshConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public final SshProvider getProvider() {
		return provider;
	}

	@Override
	public int getTimeout() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public final void init(SshProvider provider) {
		this.provider = provider;
	}

	@Override
	public SshPortForward localForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		SshPortForward fwd = createLocalForward(localBindAddress, localBindPort, targetAddress, targetPort);
		fwd.open();
		return fwd;
	}

	@Override
	public SshPublicKeySubsystem publicKeySubsystem() throws SshException {
		SshPublicKeySubsystem ks = createPublicKeySubsystem();
		ks.open();
		return ks;
	}

	@Override
	public SshPortForward remoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress, int targetPort)
			throws SshException {
		SshPortForward fwd = createRemoteForward(remoteBindAddress, remoteBindPort, targetAddress, targetPort);
		fwd.open();
		return fwd;
	}

	@Override
	public void removeChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	@Override
	public synchronized void removePortForwardListener(SshPortForwardListener listener) {
		portForwardlisteners.remove(listener);
	}

	@Override
	public final SshSCPClient scp() throws SshException {
		SshSCPClient scp = createSCP();
		scp.open();
		return scp;
	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final SftpClient sftp() throws SshException {
		SftpClient client = createSftp();
		client.open();
		return client;
	}

	@Override
	public final SshShell shell() throws SshException {
		return shell(null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshShell shell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		SshShell shell = createShell(termType, cols, rows, pixWidth, pixHeight, terminalModes);
		shell.open();
		return shell;
	}

	@Override
	public Future<SshShell> shellLater() {
		return shellLater(null, 0, 0, 0, 0, null);
	}

	@Override
	public Future<SshShell> shellLater(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes) {
		return new AbstractFuture<SshShell>() {
			{
				getProvider().getExecutor().execute(createRunnable());
			}

			@Override
			SshShell doFuture() throws Exception {
				return shell(termType, cols, rows, pixWidth, pixHeight, terminalModes);
			}
		};
	}

	@Override
	public Future<Void> closeLater() {
		return new AbstractFuture<Void>() {
			@Override
			Void doFuture() throws Exception {
				close();
				return null;
			}
		};
	}

	protected void checkConnectedAndAuthenticated() throws SshException {
		if (!isConnected())
			throw new SshException(SshException.NOT_OPEN, String.format("Not connected."));
		if (!isAuthenticated())
			throw new SshException(SshException.NOT_AUTHENTICATED, String.format("Not connected."));
	}

	protected Map<String, SshAuthenticator> createAuthenticatorMap(SshAuthenticator[] authenticators) {
		Map<String, SshAuthenticator> authenticatorMap = new HashMap<>();
		for (int i = 0; i < authenticators.length; i++) {
			if (authenticatorMap.containsKey(authenticators[i].getTypeName())) {
				throw new IllegalArgumentException(
						"Two authenticators using the name '" + authenticators[i].getTypeName() + "' have been provided.");
			}
			authenticatorMap.put(authenticators[i].getTypeName(), authenticators[i]);
		}
		return authenticatorMap;
	}

	protected abstract void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException;

	protected SshCommand doCreateCommand(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		throw new UnsupportedOperationException("Commands are not supported in this implementation.");
	}

	protected SshPortForward doCreateLocalForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		throw new UnsupportedOperationException("Local forwarding is not supported in this implementation.");
	}

	protected SshPublicKeySubsystem doCreatePublicKeySubsystem() throws SshException {
		throw new UnsupportedOperationException();
	}

	protected SshPortForward doCreateRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		throw new UnsupportedOperationException("Remote forwarding is not supported in this implementation.");
	}

	protected SshSCPClient doCreateSCP() throws SshException {
		throw new UnsupportedOperationException("SCP is is not supported in this implementation.");
	}

	protected SftpClient doCreateSftp() throws SshException {
		throw new UnsupportedOperationException("SFTP is is not supported in this implementation.");
	}

	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		throw new UnsupportedOperationException("Shell is is not supported in this implementation.");
	}

	protected void firePortForwardChannelClosed(int type, SshPortForwardTunnel channel) {
		for (int i = portForwardlisteners.size() - 1; i >= 0; i--)
			portForwardlisteners.get(i).channelClosed(type, channel);
	}

	protected void firePortForwardChannelOpened(int type, SshPortForwardTunnel channel) {
		for (int i = portForwardlisteners.size() - 1; i >= 0; i--)
			portForwardlisteners.get(i).channelOpened(type, channel);
	}

	protected abstract void onClose() throws SshException;

	protected void postConnect() throws SshException {
		if (getProvider().getCapabilities().contains(Capability.AGENT) && getConfiguration().getAgent() != null)
			addChannelHandler(getConfiguration().getAgent());
	}
}
