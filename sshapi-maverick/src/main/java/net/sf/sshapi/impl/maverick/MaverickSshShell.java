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
package net.sf.sshapi.impl.maverick;

import com.sshtools.ssh.ChannelEventListener;
import com.sshtools.ssh.SshSession;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.SshShellListener;

class MaverickSshShell extends AbstractMaverickSshStreamChannel<SshShellListener, SshShell>
		implements SshShell, ChannelEventListener {
	MaverickSshShell(SshProvider provider, SshConfiguration configuration, SshSession session) {
		super(provider, configuration, session);
	}

	@Override
	public void onChannelOpen() throws SshException {
		try {
			if (!((SshSession) getChannel()).startShell()) {
				throw new SshException("Failed to start shell");
			}
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException("Failed to open shell.", e);
		}
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		try {
			((SshSession) getChannel()).changeTerminalDimensions(width, height, pixw, pixw);
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}
}
