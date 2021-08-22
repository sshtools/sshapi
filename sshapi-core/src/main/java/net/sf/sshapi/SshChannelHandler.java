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

import net.sf.sshapi.SshCustomChannel.ChannelData;

/**
 * SSH allows custom channels to be handled. Implement this interface, and add
 * the handler to the {@link SshClient} instance. When the server requests a
 * channel with this name, {@link #channelCreated(SshCustomChannel)} will be called.
 * 
 */
public interface SshChannelHandler {

	/**
	 * Get the names of the channels this factory supports.
	 * 
	 * @return channel names
	 */
	String[] getSupportChannelNames();

	/**
	 * Called when a new channel with a supported name has been created.
	 * 
	 * @param channelName channel name
	 * @param requestData request data
	 * @return data to send or <code>null</code> for none
	 */
	ChannelData createChannel(String channelName, byte[] requestData);

	/**
	 * Called when a new channel with a supported name has been created. The client code
	 * can hook in here to add listeners.
	 * 
	 * @param channel channel
	 * @throws IOException on error
	 */
	void channelCreated(SshCustomChannel channel) throws IOException;
}
