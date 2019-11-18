package net.sf.sshapi.sftp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SftpHandleOutputStream extends OutputStream {
	/**
	 * 
	 */
	private final ByteBuffer buf;
	private final SftpHandle h;

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