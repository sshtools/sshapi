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
package net.sf.sshapi.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import net.sf.sshapi.AbstractSocket;
import net.sf.sshapi.SshCustomChannel;
import net.sf.sshapi.SshClient;

/**
 * The Class RemoteSocket.
 */
public class RemoteSocket extends AbstractSocket {
	
	/** The channel. */
	private SshCustomChannel channel;
	
	/** The client. */
	private SshClient client;

	/**
	 * Instantiates a new remote socket.
	 *
	 * @param client the client
	 */
	RemoteSocket(SshClient client) {
		super();
		this.client = client;
	}

	/**
	 * Instantiates a new remote socket.
	 *
	 * @param client the client
	 * @param host the host
	 * @param port the port
	 * @throws UnknownHostException the unknown host exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	RemoteSocket(SshClient client, String host, int port) throws UnknownHostException, IOException {
		super();
		this.client = client;
		this.connect(new InetSocketAddress(host, port));
	}

	/**
	 * Bind.
	 *
	 * @param bindpoint the bindpoint
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Do close.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the input stream.
	 *
	 * @return the input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public InputStream getInputStream() throws IOException {
		if (!isConnected()) {
			throw new IOException("Not connected.");
		}
		return channel.getInputStream();
	}

	/**
	 * Gets the output stream.
	 *
	 * @return the output stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (!isConnected()) {
			throw new IOException("Not connected.");
		}
		return channel.getOutputStream();
	}

	/**
	 * Checks if is connected.
	 *
	 * @return true, if is connected
	 */
	@Override
	public boolean isConnected() {
		return channel != null && !isClosed();
	}

	/**
	 * On connect.
	 *
	 * @param addr the addr
	 * @param timeout the timeout
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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