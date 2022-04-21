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

import net.sf.sshapi.SshException;

/**
 * Monitors an {@link OutputStream} and fires data transfer events. Intended for
 * provides to wrap download and upload streams to get these events.
 */
public class SftpOutputStream extends OutputStream {
	private final OutputStream out;
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
	 * @param out out
	 * @param client client
	 * @param path path
	 * @param target target
	 */
	public SftpOutputStream(OutputStream out, AbstractSftpClient<?> client, String path, String target) {
		this(out, client, path, target, -1);
	}

	/**
	 * Constructor.
	 * 
	 * @param out out
	 * @param client client
	 * @param path path
	 * @param target target
	 * @param length
	 */
	public SftpOutputStream(OutputStream out, AbstractSftpClient<?> client, String path, String target, long length) {
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

	/**
	 * Get the length.
	 * 
	 * @return length
	 */
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
