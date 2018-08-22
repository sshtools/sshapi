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
package net.sf.sshapi.impl.jsch;

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelShell;

import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;

abstract class JschSshShell extends AbstractJschStreamChannel<SshChannelListener<SshShell>, SshShell>
		implements SshShell {

	private InputStream ext;

	public JschSshShell(SshConfiguration configuration, ChannelShell channel) throws SshException {
		super(configuration, channel);
	}

	@Override
	protected final void onChannelOpen() throws SshException {
		try {

			if (!Util.nullOrTrimmedBlank(getConfiguration().getX11Host())) {
				((ChannelShell) getChannel()).setXForwarding(true);
			}

			ext = ((ChannelShell) getChannel()).getExtInputStream();
		} catch (Exception ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
		onShellOpen();
	}

	protected abstract void onShellOpen() throws SshException;

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return ext;
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		try {
			((ChannelShell) getChannel()).setPtySize(width, height, pixw, pixh);
		} catch (Exception e) {
			throw new SshException(SshException.GENERAL, e);
		}
	}

}
