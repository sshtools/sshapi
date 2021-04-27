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
package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.sun.jna.Memory;

import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.Logger;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.forwarding.AbstractPortForward;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;

public class LibsshLocalForward extends AbstractPortForward implements SshPortForward, Runnable {

	private static final Logger LOG = SshConfiguration.getLogger();

	class TunnelChannel
			extends AbstractDataProducingComponent<SshLifecycleListener<SshPortForwardTunnel>, SshPortForwardTunnel>
			implements SshPortForwardTunnel {
		private SocketChannel socket;
		private ssh_channel channel;
		private Thread thread;

		public TunnelChannel(SshProvider provider, SocketChannel tunnel) {
			super(provider);
			this.socket = tunnel;
		}

		@Override
		public boolean isOpen() {
			return super.isOpen() && isSshAvailable();
		}

		private boolean isSshAvailable() {
			return library.ssh_channel_is_open(channel) == 1 && library.ssh_is_connected(libSshSession) == 1;
		}

		@Override
		public String getBindAddress() {
			return localAddress;
		}

		@Override
		public int getBindPort() {
			return boundPort;
		}

		@Override
		public String getOriginatingAddress() {
			try {
				SocketAddress addr = socket.getRemoteAddress();
				return addr instanceof InetSocketAddress ? ((InetSocketAddress) addr).getHostName() : "";
			} catch (IOException ioe) {
				throw new IllegalStateException("Failed to get bind address.", ioe);
			}
		}

		@Override
		public int getOriginatingPort() {
			try {
				SocketAddress addr = socket.getRemoteAddress();
				return addr instanceof InetSocketAddress ? ((InetSocketAddress) addr).getPort() : 0;
			} catch (IOException ioe) {
				throw new IllegalStateException("Failed to get bind address.", ioe);
			}
		}

		@Override
		public String toString() {
			return "TunnelChannel [getBindAddress()=" + getBindAddress() + ", getBindPort()=" + getBindPort()
					+ ", getOriginatingAddress()=" + getOriginatingAddress() + ", getOriginatingPort()="
					+ getOriginatingPort() + "]";
		}

		@Override
		protected void onClose() throws net.sf.sshapi.SshException {
			if (LOG.isDebug())
				LOG.debug("onClose for {0}", hashCode());

//				if (thread != null) {
//
//					if (LOG.isDebug())
//						LOG.debug("Interrupting I/O for {0}", hashCode());
//
//					thread.interrupt();
//				}
				if (isSshAvailable()) {

					if (LOG.isDebug())
						LOG.debug("Sending EOF for {0}", hashCode());
					library.ssh_channel_send_eof(channel);

					if (LOG.isDebug())
						LOG.debug("Freeing socket channel {0}", hashCode());

					library.ssh_channel_free(channel);
				} else {
					if (LOG.isDebug())
						LOG.debug("Not closing {0} as session is not connected.", hashCode());
				}
		}

		@Override
		protected void onOpen() throws net.sf.sshapi.SshException {
			if (LOG.isDebug())
				LOG.debug("Creating channel for local forward socket for {0}", hashCode());

			channel = library.ssh_channel_new(libSshSession);
			if (channel == null)
				throw new SshException(SshException.GENERAL, "Failed to create channel for local port forward.");

			if (LOG.isDebug())
				LOG.debug("Opening forward for {0} to {1}:{2}", hashCode(), remoteHost, remotePort);

			int ret = library.channel_open_forward(channel, remoteHost, remotePort,
					localAddress == null ? "localhost" : localAddress, boundPort);
			if (ret != SshLibrary.SSH_OK)
				throw new SshException(SshException.GENERAL,
						"Failed to open channel for local port forward to " + remoteHost + ":" + remotePort);

			/*
			 * Unfortunately, we cannot use non-blocking mode in libssh just yet, because of
			 * this warning :-
			 * 
			 * <strong>Warning When the channel is in EOF state, the function returns
			 * SSH_EOF.</strong>
			 * 
			 * So .. each socket has one thread for data coming back from the SSH server
			 */
			thread = new Thread("LibsshTunnelChannel" + hashCode()) {
				@Override
				public void run() {
					Memory m = new Memory(LibsshClient.FORWARDING_BUFFER_SIZE);
					try {
						if (LOG.isDebug())
							LOG.debug("Initial state for {0}:{1}, tunnel {2} is Closed: {3}", remoteHost, remotePort,
									TunnelChannel.this.hashCode(), isClosed());

						while (!isClosed()) {
							int read = 0;

							if (LOG.isDebug())
								LOG.debug("Waiting to potentially read {0} from {1}:{2} for {3}", m.size(), remoteHost,
										remotePort, TunnelChannel.this.hashCode());
//							synchronized (closeLock) {
//								if (!isOpen()) {
//									break;
//								}
//							}

							if (!isClosing() && isOpen() && library.ssh_channel_is_eof(channel) != 1)
								read = library.ssh_channel_read(channel, m, LibsshClient.FORWARDING_BUFFER_SIZE, 0);
							else
								read = 0;
							if (read < 0) {
								throw new IOException("I/O Error");
							}
							if (read == 0) {
								if (LOG.isDebug())
									LOG.debug("EOF from {0}:{1} for {2}", remoteHost, remotePort,
											TunnelChannel.this.hashCode());
								break;
							}
							ByteBuffer buf = m.getByteBuffer(0, read);
							if (LOG.isDebug())
								LOG.debug("Read {0} from {1}:{2} for {3}, writing back to socket", read, remoteHost,
										remotePort, TunnelChannel.this.hashCode());
							socket.write(buf);
						}

						if (LOG.isDebug())
							LOG.debug("Left input for {0}:{1} for {2}", remoteHost, remotePort,
									TunnelChannel.this.hashCode());

						try {
							if (LOG.isDebug())
								LOG.debug("Shutting down socket input for {0}:{1} for {2}", remoteHost, remotePort,
										TunnelChannel.this.hashCode());
							socket.shutdownInput();
						} catch (IOException e) {
						}

					} catch (IOException ioe) {
						LOG.error("Failed I/O on input from local forward channel.", ioe);
						return;
					} catch (Exception ie) {
						LOG.error("Closed?", ie);
						// Clo
						return;
					}

					LOG.debug("Finished reading from {0}:{1} for{2}", remoteHost, remotePort, hashCode());
				}
			};
			thread.setDaemon(true);
			thread.start();

			if (LOG.isDebug())
				LOG.debug("Opened for {0} to {1}:{2}", hashCode(), remoteHost, remotePort);
		}

		protected void write(ByteBuffer buffer) throws SshException {
			if (LOG.isDebug())
				LOG.debug("Writing {0} bytes to {1}", buffer.remaining(), hashCode());

			// TODO a bit convoluted, can probably send ByteBuffer all the way through?
//			synchronized (closeLock) {
			Memory mem = new Memory(buffer.remaining());
			byte[] arr = new byte[buffer.remaining()];
			buffer.get(arr);
			mem.write(0, arr, 0, arr.length);
			int written = library.channel_write(channel, mem, arr.length);
			if (written < 1) {
				throw new SshException(SshException.IO_ERROR);
			}
//			}
			if (LOG.isDebug())
				LOG.debug("Written {0} bytes to {1}", arr.length, hashCode());
		}
	}

	private ssh_session libSshSession;
	private SshLibrary library;
	private String localAddress;
	private int localPort;
	private String remoteHost;
	private int remotePort;
	private Selector selector;
	private ServerSocketChannel ssc;
	private boolean closed;
	private int boundPort;
	private Map<SocketChannel, TunnelChannel> channels = Collections.synchronizedMap(new HashMap<>());

	public LibsshLocalForward(SshProvider provider, ssh_session libSshSession, SshLibrary library, String localAddress,
			int localPort, String remoteHost, int remotePort) {
		super(provider);
		this.libSshSession = libSshSession;
		this.library = library;
		this.localAddress = localAddress;
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	@Override
	public int getBoundPort() {
		return boundPort;
	}

	@Override
	protected void onOpen() throws SshException {

		try {
			boundPort = localPort;
			if (boundPort == 0) {
				boundPort = Util.findRandomPort();
			}
			try {
				ssc = ServerSocketChannel.open();
				ssc.configureBlocking(false);
				ssc.socket().bind(new InetSocketAddress(localAddress == null ? "localhost" : localAddress, boundPort));
				selector = Selector.open();
				ssc.register(selector, ssc.validOps());
				Thread thread = new Thread(this, "LibsshLocalForward" + hashCode());
				thread.setDaemon(true);
				thread.start();
			} catch (IOException ioe) {
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		} catch (SshException sshe) {
			throw sshe;
		}
	}

	@Override
	protected void onClose() throws SshException {
		if (!closed) {
			closed = true;
			boundPort = 0;

			Exception ex = null;
			synchronized (channels) {
				for (Map.Entry<SocketChannel, TunnelChannel> en : channels.entrySet()) {
					try {
						SshConfiguration.getLogger().debug("Closing tunnel {0}", en.getValue().hashCode());
						en.getValue().close();
					} catch (Exception e) {
						SshConfiguration.getLogger().log(Level.DEBUG, "Component {0} failed to close.", e,
								en.getValue().hashCode());
						ex = e;
					} finally {
						SshConfiguration.getLogger().debug("Closed component {0}", en.getValue().hashCode());
					}
				}
			}
			if (ex != null) {
				if (ex instanceof RuntimeException)
					throw (RuntimeException) ex;
				else if (ex instanceof RuntimeException)
					throw new SshException("Failed to close.", ex);
			}
		}
	}

	@Override
	public void run() {

		int BUFFER_SIZE = LibsshClient.FORWARDING_BUFFER_SIZE;
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		try {
			while (!closed) {
				selector.select();
				Set<?> keys = selector.selectedKeys();
				Iterator<?> i = keys.iterator();

				while (i.hasNext()) {
					SelectionKey key = (SelectionKey) i.next();

					i.remove();
					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						SocketChannel socket = ssc.accept();
						if (LOG.isDebug())
							LOG.debug("Accepted {0}", socket.getRemoteAddress());
						socket.configureBlocking(false);
						socket.register(selector, SelectionKey.OP_READ);
//						client.register(selector, SelectionKey.OP_WRITE);

						TunnelChannel tunnel = new TunnelChannel(provider, socket);
						tunnel.open();

						channels.put(socket, tunnel);
					}

					else if (key.isReadable()) {

						SocketChannel socket = (SocketChannel) key.channel();

						if (LOG.isDebug())
							LOG.debug("Key is readable from {0}", socket.getRemoteAddress());

						TunnelChannel tunnel = channels.get(socket);
						if (tunnel == null)
							LOG.warn("Got read selector for unknown socket!");
						else {
							if (LOG.isDebug())
								LOG.debug("Readable bytes from {0}", socket.getRemoteAddress());

							try {
								buffer.clear();
								int read = socket.read(buffer);
								if (read == -1) {
									if (LOG.isDebug())
										LOG.debug("Got EOF from {0}, sending on to socket", socket.getRemoteAddress());
									tunnel.close();
									socket.close();
									channels.remove(socket);
								} else {
									if (LOG.isDebug())
										LOG.debug("Read {0} bytes from {1}", read, socket.getRemoteAddress());
									buffer.flip();
									tunnel.write(buffer);
								}
							} catch (Exception e) {
								LOG.error("Failed to read from socket.", e);
								socket.close();
							}
						}
					}
				}
			}

		} catch (IOException ioe) {
			LOG.error("Local forward I/O loop ended.", ioe);
		}
	}
}
