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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

/**
 * Abstract implementation of a {@link Socket}, used by the Tunneled Sockets
 * feature of SSHAPI and it's providers.
 * 
 */
public abstract class AbstractSocket extends Socket {

	private boolean closed;
	private String host;
	private int port;
	private boolean shutIn;
	private boolean shutOut;

	/**
	 * Constructor
	 */
	public AbstractSocket() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param host host
	 * @param port port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public AbstractSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
		this.host = host;
		this.port = port;
	}

	/**
	 * Constructor.
	 * 
	 * @param host host
	 * @param port port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public AbstractSocket(InetAddress host, int port) throws UnknownHostException, IOException {
		super(host, port);
		this.host = host.getHostName();
		this.port = port;
	}

	public void bind(SocketAddress bindpoint) throws IOException {
		throw new UnsupportedOperationException();
	}

	public final void connect(SocketAddress endpoint, int timeout) throws IOException {

		if (!(endpoint instanceof InetSocketAddress)) {
			throw new IOException("Not an InetSocketAddress");
		}
		if (isConnected()) {
			throw new SocketException("Socket is already connected");
		}
		if (isClosed()) {
			throw new SocketException("Socket is closed");
		}
		onConnect((InetSocketAddress) endpoint, timeout);
	}

	protected abstract void onConnect(InetSocketAddress endpoint, int timeout) throws IOException;

	public final synchronized void close() throws IOException {
		try {
			doClose();
		} finally {
			closed = true;
		}
	}

	protected abstract void doClose() throws IOException;

	public boolean isBound() {
		return false;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isInputShutdown() {
		return shutIn;
	}

	public boolean isOutputShutdown() {
		return shutOut;
	}

	public void sendUrgentData(int data) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void setKeepAlive(boolean on) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setOOBInline(boolean on) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
		throw new UnsupportedOperationException();
	}

	public synchronized void setReceiveBufferSize(int size) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setReuseAddress(boolean on) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public synchronized void setSendBufferSize(int size) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setSoLinger(boolean on, int linger) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public synchronized void setSoTimeout(int timeout) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setTcpNoDelay(boolean on) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void setTrafficClass(int tc) throws SocketException {
		throw new UnsupportedOperationException();
	}

	public void shutdownInput() throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isInputShutdown())
			throw new SocketException("Socket input is already shutdown");
		getInputStream().close();
		shutIn = true;
	}

	public void shutdownOutput() throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isOutputShutdown())
			throw new SocketException("Socket output is already shutdown");
		getOutputStream().close();
		shutOut = true;
	}

	public SocketChannel getChannel() {
		return null;
	}

	public InetAddress getInetAddress() {
		return null;
	}

	public boolean getKeepAlive() throws SocketException {
		return false;
	}

	public boolean getOOBInline() throws SocketException {
		return false;
	}

	public int getPort() {
		return port;
	}

	public synchronized int getReceiveBufferSize() throws SocketException {
		return 0;
	}

	public boolean getReuseAddress() throws SocketException {
		return false;
	}

	public synchronized int getSendBufferSize() throws SocketException {
		return 0;
	}

	public int getSoLinger() throws SocketException {
		return 0;
	}

	public synchronized int getSoTimeout() throws SocketException {
		return 0;
	}

	public boolean getTcpNoDelay() throws SocketException {
		return false;
	}

	public int getTrafficClass() throws SocketException {
		return 0;
	}

	public String toString() {
		return "SSH Socket to " + host + ":" + port;
	}
}
