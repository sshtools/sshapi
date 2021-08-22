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

/**
 * Interface for custom channels created by {@link SshChannelHandler}
 * implementations.
 */
public interface SshCustomChannel extends SshChannel<SshCustomChannelListener, SshCustomChannel> {

	/**
	 * Encapsulate details needed for channel creation
	 */
	public interface ChannelData {
		/**
		 * Get the request data
		 * 
		 * @return request data
		 */
		byte[] getRequestData();

		/**
		 * Get the window size.
		 * 
		 * @return window size
		 */
		int getWindowSize();

		/**
		 * Get the packet size.
		 * 
		 * @return package size
		 */
		int getPacketSize();

		/**
		 * Get the timeout to use for opening the channel.
		 * 
		 * @return timeout
		 */
		long getTimeout();

		/**
		 * Called after the channel has been created by the factory. There is no
		 * need to call this method directly, but it can be overridden to return
		 * data that should be returned in the SSH_MSG_CHANNEL_OPEN_CONFIRMATION
		 * message.
		 * 
		 * @return data that should be returned in the
		 *         SSH_MSG_CHANNEL_OPEN_CONFIRMATION message
		 * @throws IOException
		 */
		byte[] create();
	}
	
	/**
	 * Get the channel name
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Get the {@link ChannelData} that was used to create this channel
	 * 
	 * @return channel data
	 */
	ChannelData getChannelData();

	/**
	 * Sends a channel request. Many channels have extensions that are specific
	 * to that particular channel type, an example of which is requesting a
	 * pseudo terminal from an interactive session.
	 * 
	 * @param requesttype the name of the request, for example "pty-req"
	 * @param wantreply specifies whether the remote side should send a
	 *            success/failure message
	 * @param requestdata the request data
	 * @return <code>true</code> if the request succeeded and wantreply=true,
	 *         otherwise <code>false</code>
	 * @throws SshException
	 */
	boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException;
}
