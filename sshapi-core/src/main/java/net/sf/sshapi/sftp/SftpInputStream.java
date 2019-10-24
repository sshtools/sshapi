package net.sf.sshapi.sftp;

import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.SshException;

/**
 * Monitors an {@link InputStream} and fires data transfer events. Intended for
 * provides to wrap download and upload streams to get these events.
 */
public class SftpInputStream extends InputStream {
	private final InputStream in;
	private final AbstractSftpClient client;
	private final String path;
	private final String target;
	private boolean started;
	private boolean closed;
	private long length;
	private long progress;

	public SftpInputStream(InputStream in, AbstractSftpClient client, String path, String target) {
		this(in, client, path, target, -1);
	}

	public SftpInputStream(InputStream in, AbstractSftpClient client, String path, String target, long length) {
		this.in = in;
		this.client = client;
		this.path = path;
		this.target = target;
		this.length = length;
	}

	@Override
	public int read() throws IOException {
		checkStarted();
		int r = in.read();
		if(r != -1) {
			progress++;
			client.fireFileTransferProgressed(path, target, progress);
		}
		return r;
	}

	@Override
	public int read(byte[] b) throws IOException {
		checkStarted();
		int r = in.read(b);
		if(r != -1) {
			progress += r;
			client.fireFileTransferProgressed(path, target, progress);
		}
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		checkStarted();
		int r = in.read(b, off, len);
		if(r != -1) {
			progress += r;
			client.fireFileTransferProgressed(path, target, progress);
		}
		return r;
	}

	@Override
	public void close() throws IOException {
		checkStarted();
		if (!closed) {
			client.fireFileTransferFinished(path, target);
			try {
				in.close();
			} finally {
				closed = true;
			}
		}
	}

	@Override
	public String toString() {
		return in.toString();
	}

	@Override
	public long skip(long n) throws IOException {
		return in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public synchronized void mark(int readlimit) {
		in.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	protected void checkStarted() {
		if (!started) {
			if (length == -1) {
				try {
					length = client.stat(path).getSize();
				} catch (SshException se) {
					throw new IllegalStateException("Failed to get size for transfer.", se);
				}
			}
			client.fireFileTransferStarted(path, target, length);
			started = true;
		}
	}
}
