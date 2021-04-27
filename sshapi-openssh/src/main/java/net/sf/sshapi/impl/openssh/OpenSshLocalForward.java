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

import net.sf.sshapi.SshException;
import net.sf.sshapi.forwarding.AbstractPortForward;

class OpenSshLocalForward extends AbstractPortForward {
	private ProcessBuilder pb;
	private Process process;
	private OpenSshClient client;

	OpenSshLocalForward(OpenSshClient client, ProcessBuilder pb) {
		super(client.getProvider());
		this.pb = pb;
		this.client = client;
	}

	@Override
	protected void onClose() throws SshException {
		try {
			process.destroy();
		} finally {
			process = null;
		}
	}

	@Override
	protected void onOpen() throws SshException {
		try {
			process = client.setupAuthentication(pb).start();
			try {
				Thread.sleep(3000);
				if (process.exitValue() != 0)
					throw new SshException(SshException.AUTHENTICATION_FAILED);
			} catch (IllegalThreadStateException | InterruptedException e) {
			}
		} catch (SshException e) {
			throw e;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		}
	}
}
