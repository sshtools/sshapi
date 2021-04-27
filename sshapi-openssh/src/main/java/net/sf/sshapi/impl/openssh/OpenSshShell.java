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
package net.sf.sshapi.impl.openssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

class OpenSshShell extends AbstractSshExtendedChannel<SshChannelListener<SshShell>, SshShell> implements SshShell {
	private ProcessBuilder pb;
	private Process process;
	private final String termType;
	private OpenSshClient client;

	OpenSshShell(OpenSshClient client, ProcessBuilder pb, String termType) {
		super(client.getProvider(), client.getConfiguration());
		this.pb = pb;
		this.termType =termType;
		this.client = client;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return process.getErrorStream();
	}

	@Override
	public int exitCode() throws IOException {
		return process.exitValue();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return process.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return process.getOutputStream();
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void onCloseStream() throws SshException {
		try {
			int result = process.waitFor();
			if (result != 0)
				SshConfiguration.getLogger().warn("Ssh client exited with non-zero code {0}", result);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			process = null;
		}
	}

	@Override
	protected final void onOpenStream() throws SshException {
		try {
			if(termType != null && termType.length() > 0) {
				pb.environment().put("TERM", termType);
				pb.command().add(3, "-t");
			}
			else
				pb.command().add(3, "-T");
			process = client.setupAuthentication(pb).start();
			try {
				if (process.waitFor(3, TimeUnit.SECONDS))
					throw new SshException(SshException.AUTHENTICATION_FAILED);
			} catch (IllegalThreadStateException | InterruptedException e) {
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		}
	}
}