package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.sun.jna.Memory;

import net.sf.sshapi.SshException;
import net.sf.sshapi.forwarding.AbstractPortForward;
import net.sf.sshapi.forwarding.SshPortForward;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;

public class LibsshLocalForward extends AbstractPortForward implements SshPortForward, Runnable {

	private ssh_session libSshSession;
	private SshLibrary library;
	private String localAddress;
	private int localPort;
	private String remoteHost;
	private ssh_channel channel;
	private int remotePort;
	private Selector selector;
	private ServerSocketChannel ssc;
	private boolean closed;

	public LibsshLocalForward(ssh_session libSshSession, SshLibrary library, String localAddress, int localPort, String remoteHost,
			int remotePort) {
		this.libSshSession = libSshSession;
		this.library = library;
		this.localAddress = localAddress;
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	@Override
	protected void onOpen() throws SshException {
		channel = library.ssh_channel_new(libSshSession);
		if (channel == null) {
			throw new SshException(SshException.GENERAL, "Failed to create channel for local port forward.");
		}

		try {
			int ret = library.channel_open_forward(channel, remoteHost, remotePort, localAddress, localPort);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL, "Failed to open channel for local port forward to " + remoteHost + ":"
					+ remotePort);
			}
			try {
				ssc = ServerSocketChannel.open();
				ssc.configureBlocking(false);
				ssc.socket().bind(new InetSocketAddress(localAddress, localPort));
				selector = Selector.open();
				ssc.register(selector, SelectionKey.OP_ACCEPT);
				new Thread(this).start();
			} catch (IOException ioe) {
				library.channel_close(channel);
				throw new SshException(SshException.IO_ERROR, ioe);
			}
		} catch (SshException sshe) {
			library.ssh_channel_free(channel);
			throw sshe;
		}
	}

	@Override
	protected void onClose() throws SshException {
		closed = true;
		library.channel_close(channel);
		library.ssh_channel_free(channel);
	}

	public void run() {
		try {
			while (!closed) {
				selector.select();
				// Get keys
				Set<?> keys = selector.selectedKeys();
				Iterator<?> i = keys.iterator();

				// For each keys...
				while (i.hasNext()) {
					SelectionKey key = (SelectionKey) i.next();

					// Remove the current key
					i.remove();

					// if isAccetable = true
					// then a client required a connection
					if (key.isAcceptable()) {
						// get client socket channel
						SocketChannel client = ssc.accept();
						// Non Blocking I/O
						client.configureBlocking(false);
						// recording to the selector (reading)
						client.register(selector, SelectionKey.OP_READ);
						client.register(selector, SelectionKey.OP_WRITE);
						continue;
					}

					// if isReadable = true
					// then the server is ready to read
					if (key.isReadable()) {

						SocketChannel client = (SocketChannel) key.channel();

						// Read byte coming from the client
						int BUFFER_SIZE = LibsshClient.FORWARDING_BUFFER_SIZE;
						ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
						try {
							client.read(buffer);
						} catch (Exception e) {
							// client is no longer active
							e.printStackTrace();
							continue;
						}

						// Write the bytes to the thunnel
						buffer.flip();
						byte[] buf = buffer.array();
						Memory mem = new Memory(buf.length);
						library.channel_write(channel, mem, buf.length);
						continue;
					}
				}

				/*
				 * Now data coming from the tunnel, write that out to the client
				 */
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
