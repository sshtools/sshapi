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
package net.sf.sshapi.sftp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * {@link OutputStream} that writes sequentially to an {@link SftpHandle}.
 */
public class SftpHandleOutputStream extends OutputStream {
	/**
	 * 
	 */
	private final ByteBuffer buf;
	private final SftpHandle h;

	/**
	 * Constructor.
	 * 
	 * @param abstractSftpClient client
	 * @param buf buffer
	 * @param h handle
	 */
	public SftpHandleOutputStream(AbstractSftpClient abstractSftpClient, ByteBuffer buf, SftpHandle h) {
		this.buf = buf;
		this.h = h;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		buf.clear();
		buf.put(b, off, len);
		buf.flip();
		h.write(buf);
		h.position(h.position() + len);
	}

	@Override
	public void close() throws IOException {
		h.close();
	}

	@Override
	public void write(int b) throws IOException {
		buf.rewind();
		buf.put((byte) b);
		buf.flip();
		h.write(buf);
		h.position(h.position() + 1);
	}
}