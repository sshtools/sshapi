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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.SshStreamChannel;

/**
 * Ensures an {@link EOFException} is thrown if {@link #available()} is called
 * when a channel is closed. This helps an optimisation used in UniTTY to work
 * correctly and is valid according to Java API documentation (available can
 * either return 0 or throw exception when at end of stream, we need exception).
 */
public class SshChannelInputStream extends FilterInputStream {

	private SshStreamChannel<?, ?> channel;

	/**
	 * Constructor.
	 * 
	 * @param in original stream
	 * @param channel channel to derive open status from
	 */
	public SshChannelInputStream(InputStream in, SshStreamChannel<?, ?> channel) {
		super(in);
		this.channel = channel;
	}

	public int available() throws IOException {
		if (!channel.isOpen())
			throw new EOFException();

		return super.available();
	}
}
