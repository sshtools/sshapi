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
package net.sf.sshapi.impl.trilead;

import java.io.IOException;
import java.io.InputStream;

import com.trilead.ssh2.Session;

import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;

class TrileadSshShell extends AbstractTrileadStreamChannel<SshChannelListener<SshShell>, SshShell> implements SshShell {
	public TrileadSshShell(SshProvider provider, SshConfiguration configuration, Session channel) throws SshException {
		super(provider, configuration, channel);
	}

	public InputStream getExtendedInputStream() throws IOException {
		return ((Session) getSession()).getStderr();
	}

	public void onChannelOpen() throws SshException {
		try {
			Session session = (Session) getSession();
			session.startShell();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		throw new UnsupportedOperationException("Trilead does not support changing of window size.");

	}
}
