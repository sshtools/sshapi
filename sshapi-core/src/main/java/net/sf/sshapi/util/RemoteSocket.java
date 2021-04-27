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