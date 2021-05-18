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
package net.sf.sshapi.impl.sshj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.schmizz.sshj.connection.channel.direct.PTYMode;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.TransportException;
import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

public class SSHJSshShell extends AbstractSshExtendedChannel<SshChannelListener<SshShell>, SshShell>
		implements SshShell {
	private final String termType;
	private Session session;
	private Shell shell;
	private int rows;
	private int cols;
	private int height;
	private int width;
	private byte[] modes;

	SSHJSshShell(SSHJSshClient client, Session session, String termType, int cols, int rows, int width, int height,
			byte[] modes) {
		super(client.getProvider(), client.getConfiguration());
		this.modes = modes;
		this.session = session;
		this.termType = termType;
		this.cols = cols;
		this.rows = rows;
		this.height = height;
		this.width = width;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return shell.getErrorStream();
	}

	@Override
	public int exitCode() throws IOException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return shell.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return shell.getOutputStream();
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		try {
			shell.changeWindowDimensions(width, height, pixw, pixh);
		} catch (TransportException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	protected void onCloseStream() throws SshException {
		try {
			shell.close();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		} finally {
			try {
				session.close();
			} catch (Exception e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
	}

	@Override
	protected final void onOpenStream() throws SshException {
		try {
			session.allocatePTY(termType, cols, rows, width, height, toModeMap(modes));
			shell = session.startShell();
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		}
	}

	public static Map<PTYMode, Integer> toModeMap(byte[] modes) {
		Map<PTYMode, Integer> modeMap = new HashMap<>();
		if (modes != null) {
			for (int i = 0; i < modes.length; i += 5) {
				modeMap.put(fromOpCode(modes[i]),
						(modes[i + 1] << 24) | (modes[i + 2] << 16) | (modes[i + 3] << 8) | (modes[i + 4]));
			}
		}
		return modeMap;
	}

	private static PTYMode fromOpCode(byte b) {
		/* TODO: precache these? */
		for (PTYMode m : PTYMode.values()) {
			if (m.getOpcode() == b) {
				return m;
			}
		}
		throw new IllegalArgumentException(String.format("No PTY mode for %d", b));
	}
}