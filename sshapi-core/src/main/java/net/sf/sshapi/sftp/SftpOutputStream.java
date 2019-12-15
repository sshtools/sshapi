package net.sf.sshapi.sftp;

import java.io.IOException;
import java.io.OutputStream;

import net.sf.sshapi.SshException;

/**
 * Monitors an {@link OutputStream} and fires data transfer events. Intended for
 * provides to wrap download and upload streams to get these events.
 */
public class SftpOutputStream extends OutputStream {
	private final OutputStream out;
	private final AbstractSftpClient client;
	private final String path;
	private final String target;
	private boolean started;
	private boolean closed;
	private long length;
	private long progress;

	public SftpOutputStream(OutputStream out, AbstractSftpClient client, String path, String target) {
		this(out, client, path, target, -1);
	}

	public SftpOutputStream(OutputStream out, AbstractSftpClient client, String path, String target, long length) {
		this.out = out;
		this.client = client;
		this.path = path;
		this.target = target;
		this.length = length;
		if (this.length == -1) {
			try {
				this.length = client.stat(path).getSize();
			} catch (SshException se) {
				throw new IllegalStateException("Failed to get size for transfer.", se);
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		checkStarted();
		out.write(b);
		progress++;
		client.fireFileTransferProgressed(path, target, progress);
	}

	@Override
	public void write(byte[] b) throws IOException {
		checkStarted();
		out.write(b);
		progress += b.length;
		client.fireFileTransferProgressed(path, target, progress);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkStarted();
		out.write(b, off, len);
		progress += len;
		client.fireFileTransferProgressed(path, target, progress);
	}

	@Override
	public void flush() throws IOException {
		checkStarted();
		out.flush();
	}

	@Override
	public void close() throws IOException {
		checkStarted();
		if (!closed) {
			client.fireFileTransferFinished(path, target);
			try {
				out.close();
			} finally {
				closed = true;
			}
		}
	}

	public long getLength() {
		return length;
	}

	@Override
	public String toString() {
		return out.toString();
	}

	protected void checkStarted() {
		if (!started) {
			client.fireFileTransferStarted(path, target, length);
			started = true;
		}
	}
}
