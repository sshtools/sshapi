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

/**
 * Interface to be implemented by listeners who wish to receive events about
 * {@link SshLifecycleComponent} opening, closing and data transfer.
 * <p>
 * Note, to date, not all providers will support all of these events.
 * 
 * @param <C> component type
 */
public interface SshDataListener<C extends SshDataProducingComponent<?,?>> {

	/**
	 * Data was sent to the server on this channel
	 * 
	 * @see #data(int, byte[], int, int)
	 */
	public final static int SENT = 0;
	/**
	 * Data was received from the server on this channel
	 * 
	 * @see #data(int, byte[], int, int)
	 */
	public final static int RECEIVED = 1;
	/**
	 * Extended data was received from the server on this channel. Only applies
	 * for channels that support it.
	 * 
	 * @see #data(int, byte[], int, int)
	 */
	public final static int EXTENDED = 2;

	/**
	 * Data has traveled over this channel.
	 * 
	 * @param channel the channel data is traveling over.
	 * @param direction will be one of {@link #SENT} or {@link #RECEIVED}
	 * @param buf buffer
	 * @param off buffer offset
	 * @param len buffer length
	 */
	void data(C channel, int direction, byte[] buf, int off, int len);
}
