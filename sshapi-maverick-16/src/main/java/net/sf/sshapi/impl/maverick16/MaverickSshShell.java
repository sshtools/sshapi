/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.impl.maverick16;

import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

import com.maverick.ssh.SshClient;
import com.maverick.ssh.SshSession;

class MaverickSshShell extends MaverickSshStreamChannel implements SshShell {

	MaverickSshShell(SshSession session) {
		super(session);
	}

	public InputStream getExtendedInputStream() throws IOException {
		return ((SshSession) getChannel()).getStderrInputStream();
	}

	public void onChannelOpen() throws SshException {
		try {
			if (!((SshSession) getChannel()).startShell()) {
				throw new SshException("Failed to start shell");
			}
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException("Failed to open shell.", e);
		}
	}

	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		try {
			((SshSession) getChannel()).changeTerminalDimensions(width, height, pixw, pixw);
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}

	}
}
