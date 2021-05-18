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
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;

/**
 * libssh shell implementation.
 */
public class LibsshShell extends AbstractSshExtendedChannel<SshChannelListener<SshShell>, SshShell> implements SshShell {
	private InputStream in;
	private InputStream ext;
	private OutputStream out;
	private ssh_channel channel;
	private SshLibrary library;
	private String termType;
	private int cols;
	private int rows;
	private boolean useExtendedStream;
	private ssh_session libSshSession;

	/**
	 * Constructor.
	 * 
	 * @param provider provider
	 * @param configuration configuration
	 * @param libSshSession session
	 * @param library library
	 * @param termType term type
	 * @param cols columns 
	 * @param rows rows
	 * @param useExtendedStream use extended stream
	 */
	public LibsshShell(SshProvider provider, SshConfiguration configuration, ssh_session libSshSession, SshLibrary library,
			String termType, int cols, int rows, boolean useExtendedStream) {
		super(provider, configuration);
		this.libSshSession = libSshSession;
		this.library = library;
		this.termType = termType;
		this.cols = cols;
		this.rows = rows;
		this.useExtendedStream = useExtendedStream;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (in == null) {
			throw new IllegalStateException(
					"Shell channel not opened. If you use SshClient.createShell(), you must also call SshShell.open(). Alternatively use SshClient.shell() which will return an opened shell.");
		}
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (in == null) {
			throw new IllegalStateException(
					"Shell channel not opened. If you use SshClient.createShell(), you must also call SshShell.open(). Alternatively use SshClient.shell() which will return an opened shell.");
		}
		return out;
	}

	@Override
	public final void onOpenStream() throws SshException {
		channel = library.ssh_channel_new(libSshSession);
		if (channel == null) {
			throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to open channel for shell.");
		}
		try {
			int ret = library.ssh_channel_open_session(channel);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to open session for shell channel.");
			}
			if (termType != null) {
				ret = library.ssh_channel_request_pty_size(channel, termType, cols, rows);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to set PTY size");
				}
			}
			try {
				ret = library.ssh_channel_request_shell(channel);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.FAILED_TO_OPEN_SHELL);
				}
				in = new EventFiringInputStream(new LibsshInputStream(library, channel, false), SshDataListener.RECEIVED);
				if (useExtendedStream) {
					ext = new EventFiringInputStream(new LibsshInputStream(library, channel, true), SshDataListener.EXTENDED);
				}
				out = new EventFiringOutputStream(new LibsshOutputStream(library, channel));
			} catch (SshException sshe) {
				library.ssh_channel_close(channel);
				throw sshe;
			}
		} catch (SshException sshe) {
			library.ssh_channel_free(channel);
			throw sshe;
		}
	}

	@Override
	public void onCloseStream() throws SshException {
		library.ssh_channel_send_eof(channel);
		try {
			in.close();
		} catch (IOException e) {
		}
		try {
			out.close();
		} catch (IOException e) {
		}
		if (ext != null) {
			try {
				ext.close();
			} catch (IOException e) {
			}
		}
		if (channel != null) {
			library.ssh_channel_close(channel);
			library.ssh_channel_free(channel);
		}
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return ext;
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		int ret = library.ssh_channel_change_pty_size(channel, width, height);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException("Failed to change terminal size. Err:" + ret);
		}
	}

	@Override
	public int exitCode() throws IOException {
		return library.ssh_channel_get_exit_status(channel);
	}
}