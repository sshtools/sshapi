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

import net.sf.sshapi.SshCustomChannel.ChannelData;

/**
 * A basic {@link ChannelData} implementation.
 */
public class DefaultChannelData implements ChannelData {
	
	/** The window size. */
	int windowSize;
	
	/** The packet size. */
	int packetSize;
	
	/** The timeout. */
	long timeout;
	
	/** The create. */
	byte[] create;
	
	/** The request data. */
	byte[] requestData;

	/**
	 * Instantiates a new default channel data.
	 *
	 * @param windowSize the window size
	 * @param packetSize the packet size
	 * @param timeout the timeout
	 * @param requestData the request data
	 */
	public DefaultChannelData(int windowSize, int packetSize, long timeout, byte[] requestData) {
		super();
		this.windowSize = windowSize;
		this.packetSize = packetSize;
		this.timeout = timeout;
		this.requestData = requestData;
	}

	/**
	 * Instantiates a new default channel data.
	 *
	 * @param windowSize the window size
	 * @param packetSize the packet size
	 * @param timeout the timeout
	 * @param create the create
	 * @param requestData the request data
	 */
	public DefaultChannelData(int windowSize, int packetSize, long timeout, byte[] create, byte[] requestData) {
		super();
		this.windowSize = windowSize;
		this.packetSize = packetSize;
		this.timeout = timeout;
		this.create = create;
		this.requestData = requestData;
	}

	/**
	 * Gets the window size.
	 *
	 * @return the window size
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * Sets the window size.
	 *
	 * @param windowSize the new window size
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * Gets the packet size.
	 *
	 * @return the packet size
	 */
	public int getPacketSize() {
		return packetSize;
	}

	/**
	 * Sets the packet size.
	 *
	 * @param packetSize the new packet size
	 */
	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * Gets the timeout.
	 *
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * Sets the timeout.
	 *
	 * @param timeout the new timeout
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Sets the creates the.
	 *
	 * @param create the new creates the
	 */
	public void setCreate(byte[] create) {
		this.create = create;
	}

	/**
	 * Gets the request data.
	 *
	 * @return the request data
	 */
	public byte[] getRequestData() {
		return requestData;
	}

	/**
	 * Sets the request data.
	 *
	 * @param requestData the new request data
	 */
	public void setRequestData(byte[] requestData) {
		this.requestData = requestData;
	}

	/**
	 * Creates the.
	 *
	 * @return the byte[]
	 */
	public byte[] create() {
		return create;
	}

}
