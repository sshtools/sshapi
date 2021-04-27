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
package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.jna.Memory;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshConfiguration;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

public class LibsshOutputStream extends OutputStream {

	private static final Logger LOG = SshConfiguration.getLogger();
	private ssh_channel channel;
	private SshLibrary library;
	private boolean closed;

	public LibsshOutputStream(SshLibrary library, ssh_channel channel) {
		this.channel = channel;
		this.library = library;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Stream closed.");
		}
		Memory m = new Memory(len);
		m.write(0, b, off, len);
		if (LOG.isDebug())
			LOG.debug("Writing {0} bytes", len);
		int written = library.ssh_channel_write(channel, m, len);
		if (written < 1) {
			throw new IOException("Failed to write.");
		}
		if (LOG.isDebug())
			LOG.debug("Written {0} bytes", len);
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Stream closed.");
		}
		Memory m = new Memory(1);
		m.setByte(0, (byte) b);
		if (LOG.isDebug())
			LOG.debug("Writing 1 bytes");
		int written = library.ssh_channel_write(channel, m, 1);
		if (written != 1) {
			throw new IOException("Failed to write.");
		}
		if (LOG.isDebug())
			LOG.debug("Written 1 bytes");
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (!closed) {
			closed = true;
			if (LOG.isDebug())
				LOG.debug("Sending EOF");
			library.ssh_channel_send_eof(channel);
			if (LOG.isDebug())
				LOG.debug("Sent EOF, closed output stream");
		}
		else
			if (LOG.isDebug())
				LOG.debug("Request to close stream that is already closed.");
	}

}