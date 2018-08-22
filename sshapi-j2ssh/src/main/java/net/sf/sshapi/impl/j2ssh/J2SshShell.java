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
package net.sf.sshapi.impl.j2ssh;

import java.io.IOException;
import java.io.InputStream;

import com.sshtools.j2ssh.session.PseudoTerminal;
import com.sshtools.j2ssh.session.SessionChannelClient;

import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

class J2SshShell extends AbstractJ2SshStreamChannel<SshChannelListener<SshShell>, SshShell> implements SshShell {

	public J2SshShell(SshConfiguration configuration, SessionChannelClient session) {
		super(configuration, session);
	}

	public InputStream getExtendedInputStream() throws IOException {
		return getShellChannel().getStderrInputStream();
	}

	private SessionChannelClient getShellChannel() {
		return ((SessionChannelClient) getChannel());
	}

	public final void onChannelOpen() throws SshException {
		try {
			if (!getShellChannel().startShell()) {
				throw new net.sf.sshapi.SshException("Failed to start shell.");
			}
		} catch (IOException e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	public void requestPseudoTerminalChange(final int width, final int height, final int pixw, final int pixh)
			throws SshException {
		try {
			getShellChannel().changeTerminalDimensions(new PseudoTerminal() {

				public int getWidth() {
					return pixw;
				}

				public String getTerm() {
					throw new UnsupportedOperationException();
				}

				public int getRows() {
					return height;
				}

				public int getHeight() {
					return pixh;
				}

				public String getEncodedTerminalModes() {
					throw new UnsupportedOperationException();
				}

				public int getColumns() {
					return width;
				}
			});
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}
}
