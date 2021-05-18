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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * An input stream backed by a {@link SftpHandle}.
 */
public class SftpHandleInputStream extends InputStream {
	private final ByteBuffer buf;
	private final SftpHandle h;
	private long mark = -1;

	/**
	 * Constructor.
	 * 
	 * @param buf buffer
	 * @param h handle
	 */
	public SftpHandleInputStream(ByteBuffer buf, SftpHandle h) {
		this.buf = buf;
		this.h = h;
	}

	@Override
	public int read() throws IOException {
		buf.rewind();
		buf.limit(1);
		try {
			int r = h.read(buf);
			if (r != -1) {
				buf.flip();
				h.position(h.position() + 1);
				return buf.get();
			}
			return r;
		} finally {
			buf.limit(buf.capacity());
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		buf.rewind();
		buf.limit(len);
		try {
			int read = h.read(buf);
			if (read != -1) {
				buf.flip();
				buf.get(b, off, read);
				h.position(h.position() + read);
			}
			return read;
		} finally {
			buf.limit(buf.capacity());
		}
	}

	@Override
	public long skip(long n) throws IOException {
		return h.position(n).position();
	}

	@Override
	public void close() throws IOException {
		h.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		mark = readlimit;
	}

	@Override
	public synchronized void reset() throws IOException {
		if (mark != -1) {
			h.position(mark);
			mark = -1;
		}
	}

	@Override
	public boolean markSupported() {
		return true;
	}
}