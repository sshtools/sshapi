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
package net.sf.sshapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.net.SocketFactory;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.RemoteSocketFactory;
import net.sf.sshapi.util.Util;

/**
 * Abstract implementation of a {@link SshClient}. All provider client
 * implementations will probably want to extend this as it provides some basic
 * common services.
 */
public abstract class AbstractClient implements SshClient {

	public final static class ForwardingChannel
			extends AbstractSshStreamChannel<SshChannelListener<SshChannel>, SshChannel> implements SshChannel {
		private SshPortForward localForward;
		private Socket socket;
		private String hostname;
		private int port;
		private AbstractClient client;

		protected ForwardingChannel(AbstractClient client, SshProvider provider, SshConfiguration configuration,
				String hostname, int port) {
			super(provider, configuration);
			this.client = client;
			this.hostname = hostname;
			this.port = port;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			if (socket == null)
				throw new IOException("Not open.");
			return new EventFiringInputStream(socket.getInputStream(), SshDataListener.RECEIVED);
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			if (socket == null)
				throw new IOException("Not open.");
			return new EventFiringOutputStream(socket.getOutputStream());
		}

		@Override
		protected final void onOpenStream() throws SshException {
			localForward = client.localForward("127.0.0.1", 0, hostname, port);
			try {
				socket = new Socket("127.0.0.1", localForward.getBoundPort());
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, "Failed to open local socket to local forward.", e);
			}
		}

		@Override
		protected void onCloseStream() throws SshException {
			try {
				socket.close();
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, "Failed to open close socket for local forward.", e);
			} finally {
				localForward.close();
			}
		}

		@Override
		public String getName() {
			return "local-tcpip";
		}

		@Override
		public ChannelData getChannelData() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
			throw new UnsupportedOperationException();
		}
	}

	protected Set<SshLifecycleComponent<?, ?>> activeComponents = Collections.synchronizedSet(new LinkedHashSet<>());
	protected List<Thread> interruptable = Collections.synchronizedList(new ArrayList<>());
	private SshConfiguration configuration;
	private String hostname;
	private int port;
	private List<SshPortForwardListener> portForwardlisteners = new ArrayList<>();
	private List<SshClientListener> listeners = new ArrayList<>();
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
	public Set<SshLifecycleComponent<?, ?>> getAllActiveComponents() {
		return Collections.unmodifiableSet(activeComponents);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SshLifecycleComponent<?, ?>> Set<T> getActiveComponents(Class<T> clazz) {
		synchronized (activeComponents) {
			Set<T> ts = new LinkedHashSet<>();
			for (SshLifecycleComponent<?, ?> en : activeComponents) {
				if (en.getClass().isAssignableFrom(clazz))
					ts.add((T) en);
			}
			return Collections.unmodifiableSet(ts);
		}
	}

	@Override
	public void addListener(SshClientListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(SshClientListener listener) {
		listeners.remove(listener);
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
			Exception ex = null;
			try {
				synchronized (activeComponents) {
					for (SshLifecycleComponent<?, ?> c : new LinkedList<>(activeComponents)) {
						try {
							SshConfiguration.getLogger().debug("Closing component {0}", c.hashCode());
							c.close();
						} catch (Exception e) {
							SshConfiguration.getLogger().log(Level.DEBUG, "Component {0} failed to close.", e,
									c.hashCode());
							ex = e;
						} finally {
							SshConfiguration.getLogger().debug("Closed component {0}", c.hashCode());
						}
					}
					if (ex != null) {
						if (ex instanceof IOException)
							throw (IOException) ex;
						else if (ex instanceof RuntimeException)
							throw (RuntimeException) ex;
						else if (ex instanceof RuntimeException)
							throw new SshException("Failed to close.", ex);
					}
				}
			} finally {
				onClose();
			}
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
		return connectLater(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec),
				authenticators);
	}

	@Override
	public Future<Void> connectLater(String username, String hostname, int port, SshAuthenticator... authenticators) {
		return new AbstractFuture<Void>() {
			@Override
			Void doFuture() throws Exception {
				connect(username, hostname, port, authenticators);
				;
				return null;
			}
		};
	}

	@Override
	public final void connect(String spec, SshAuthenticator... authenticators) throws SshException {
		connect(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public final void connect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
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
	public final SshCommand createCommand(String command, String termType, int cols, int rows, int pixWidth,
			int pixHeight, byte[] terminalModes) throws SshException {
		checkConnectedAndAuthenticated();
		SshCommand client = doCreateCommand(command, termType, cols, rows, pixWidth, pixHeight, terminalModes);
		client.addListener(new SshChannelListener<SshCommand>() {
			@Override
			public void closed(SshCommand channel) {
				activeComponents.remove(channel);
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
		return client;
	}

	@Override
	public final SshPortForward createLocalForward(String localBindAddress, int localBindPort, String targetAddress,
			int targetPort) throws SshException {
		checkConnectedAndAuthenticated();
		SshPortForward client = doCreateLocalForward(localBindAddress, localBindPort, targetAddress, targetPort);
		client.addListener(new SshPortForwardListener() {
			@Override
			public void closed(SshPortForward channel) {
				activeComponents.remove(channel);
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
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
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
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
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
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
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
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
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
		return client;
	}

	@Override
	public final SshShell createShell() throws SshException {
		return createShell(null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		checkConnectedAndAuthenticated();
		SshShell client = doCreateShell(termType, cols, rows, pixWidth, pixHeight, terminalModes);
		client.addListener(new SshChannelListener<SshShell>() {
			@Override
			public void closed(SshShell channel) {
				activeComponents.remove(channel);
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
		return client;
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws SshException {
		return new RemoteSocketFactory(this);
	}

	@Override
	public final SshChannel createForwardingChannel(String hostname, int port) throws SshException {
		SshChannel client = doCreateForwardingChannel(hostname, port);
		client.addListener(new SshChannelListener<SshChannel>() {
			@Override
			public void closed(SshChannel channel) {
				activeComponents.remove(channel);
				fireComponentRemoved(channel);
			}
		});
		activeComponents.add(client);
		fireComponentCreated(client);
		return client;
	}

	@Override
	public final SshChannel forwardingChannel(String hostname, int port) throws SshException {
		SshChannel channel = createForwardingChannel(hostname, port);
		channel.open();
		return channel;
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
		if(provider.getCapabilities().contains(Capability.IO_TIMEOUTS)) {
			try {
				setTimeout(getConfiguration().getIoTimeout());
			} catch (IOException e) {
				SshConfiguration.getLogger().debug("Failed to set initial timeout.", e);
			}
		}
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
	public SshPortForward remoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
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
	public Future<SshShell> shellLater(String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) {
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
				throw new IllegalArgumentException("Two authenticators using the name '"
						+ authenticators[i].getTypeName() + "' have been provided.");
			}
			authenticatorMap.put(authenticators[i].getTypeName(), authenticators[i]);
		}
		return authenticatorMap;
	}

	protected abstract void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException;

	protected SshCommand doCreateCommand(String command, String termType, int cols, int rows, int pixWidth,
			int pixHeight, byte[] terminalModes) throws SshException {
		throw new UnsupportedOperationException("Commands are not supported in this implementation.");
	}

	protected SshPortForward doCreateLocalForward(String localBindAddress, int localBindPort, String targetAddress,
			int targetPort) throws SshException {
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
		return new DefaultSCPClient(this);
	}

	protected SftpClient doCreateSftp() throws SshException {
		throw new UnsupportedOperationException("SFTP is is not supported in this implementation.");
	}

	protected SshShell doCreateShell(String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		throw new UnsupportedOperationException("Shell is is not supported in this implementation.");
	}

	protected void fireComponentCreated(SshLifecycleComponent<?, ?> component) {
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).created(component);
	}

	protected void fireComponentRemoved(SshLifecycleComponent<?, ?> component) {
		for (int i = listeners.size() - 1; i >= 0; i--)
			listeners.get(i).removed(component);
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

	protected SshChannel doCreateForwardingChannel(String hostname, int port) throws SshException {
		return new ForwardingChannel(this, getProvider(), getConfiguration(), hostname, port);
	}
	
	protected void interrupt() {
		synchronized(interruptable) {
			for(Thread t : interruptable)
				t.interrupt();
		}
	}

	protected void uninterruptable() {
		interruptable.remove(Thread.currentThread());
	}

	protected void interruptable() {
		interruptable(0);		
	}
	
	protected void interruptable(long timeout) {
		interruptable.add(Thread.currentThread());		
	}

	protected void runInterruptable(Runnable callable) {
		interruptable();
		try {
			callable.run();
		}
		finally {
			uninterruptable();
		}
	}
	
	protected <T> T callInterruptable(Callable<T> callable) throws Exception {
		interruptable();
		try {
			return callable.call();
		}
		finally {
			uninterruptable();
		}
	}
}
