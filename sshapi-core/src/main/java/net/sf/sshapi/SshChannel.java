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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

/**
 * Extension to {@link SshLifecycleComponent} for channels who expose I/O
 * streams, such as executing a command, or a {@link SshShell}.
 * 
 * @param <L> listener type
 * @param <C> component type
 * 
 */
public interface SshChannel<L extends SshChannelListener<C>,
	C extends SshDataProducingComponent<L, SshDataListener<C>>>
	extends SshDataProducingComponent<L, SshDataListener<C>> {
	
	/**
	 * Set the callback that will be invoked when bytes are available on the input of this
	 * channel. This is the non-blocking variant of using {@link #getInputStream()}.
	 * 
	 *  @param input handler
	 */
	void setInput(SshInput input);

	/**
	 * Write bytes to the output of this channel without block. This is the non-blocking
	 * variant of using {@link #getOutputStream()}. 
	 * 
	 * @param buffer buffer
	 * @return future
	 */
	Future<Void> writeLater(ByteBuffer buffer);
	
	/**
	 * Get the input stream for this channel.
	 * 
	 * @return input stream
	 * @throws IOException
	 */
	InputStream getInputStream() throws IOException;

	/**
	 * Get the output stream for this channel.
	 * 
	 * @return output stream
	 * @throws IOException
	 */
	OutputStream getOutputStream() throws IOException;

}
