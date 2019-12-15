package net.sf.sshapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshClient;

public class RemoteSocket extends AbstractSocket {
	private SshChannel channel;
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
	public void onConnect(InetSocketAddress addr, int timeout) throws IOException {
		if (client == null || !client.isConnected()) {
			throw new IOException("Not connected.");
		}
		try {
			channel = client.forwardingChannel(addr.getHostName(), addr.getPort());
		} catch (Exception e) {
			IOException ioe = new IOException("Failed to open direct-tcpip channel.");
			ioe.initCause(e);
			throw ioe;
		}
	}
}