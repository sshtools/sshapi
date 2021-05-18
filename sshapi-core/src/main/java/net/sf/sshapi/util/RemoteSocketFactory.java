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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import net.sf.sshapi.SshClient;

/**
 * A factory for creating RemoteSocket objects.
 */
public class RemoteSocketFactory extends SocketFactory {
	
	/** The client. */
	private SshClient client;

	/**
	 * Instantiates a new remote socket factory.
	 *
	 * @param client the client
	 */
	public RemoteSocketFactory(SshClient client) {
		this.client = client;
	}

	/**
	 * Creates a new RemoteSocket object.
	 *
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public Socket createSocket() throws IOException {
		return new RemoteSocket(client);
	}

	/**
	 * Creates a new RemoteSocket object.
	 *
	 * @param host the host
	 * @param port the port
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return new RemoteSocket(client, host.getHostAddress(), port);
	}

	/**
	 * Creates a new RemoteSocket object.
	 *
	 * @param address the address
	 * @param port the port
	 * @param localAddress the local address
	 * @param localPort the local port
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return new RemoteSocket(client, address.getHostAddress(), port);
	}

	/**
	 * Creates a new RemoteSocket object.
	 *
	 * @param host the host
	 * @param port the port
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnknownHostException the unknown host exception
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return new RemoteSocket(client, host, port);
	}

	/**
	 * Creates a new RemoteSocket object.
	 *
	 * @param host the host
	 * @param port the port
	 * @param localHost the local host
	 * @param localPort the local port
	 * @return the socket
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws UnknownHostException the unknown host exception
	 */
	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		return new RemoteSocket(client, host, port);
	}
}