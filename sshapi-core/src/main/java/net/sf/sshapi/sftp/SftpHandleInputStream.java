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