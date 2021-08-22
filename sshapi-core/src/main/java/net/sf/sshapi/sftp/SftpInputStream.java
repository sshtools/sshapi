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

import net.sf.sshapi.SshException;

/**
 * Monitors an {@link InputStream} and fires data transfer events. Intended for
 * provides to wrap download and upload streams to get these events.
 */
public class SftpInputStream extends InputStream {
	private final InputStream in;
	private final AbstractSftpClient<?> client;
	private final String path;
	private final String target;
	private boolean started;
	private boolean closed;
	private long length;
	private long progress;

	/**
	 * Constructor.
	 * 
	 * @param in input satream
	 * @param client client
	 * @param path path
	 * @param target target
	 */
	public SftpInputStream(InputStream in, AbstractSftpClient<?> client, String path, String target) {
		this(in, client, path, target, -1);
	}

	/**
	 * Constructor.
	 * 
	 * @param in input stream
	 * @param client client
	 * @param path path
	 * @param target target
	 * @param length
	 */
	public SftpInputStream(InputStream in, AbstractSftpClient<?> client, String path, String target, long length) {
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
