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
package net.sf.sshapi.impl.jsch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;

import net.sf.sshapi.SshStreamChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;

abstract class JschSshShell extends AbstractJschStreamChannel<SshStreamChannelListener<SshShell>, SshShell>
		implements SshShell {

	private InputStream ext;

	public JschSshShell(SshProvider provider, SshConfiguration configuration, ChannelShell channel) throws SshException {
		super(provider, configuration, channel);
	}

	@Override
	protected final void onChannelOpen() throws SshException {
		try {
			try {
				if(getConfiguration().getSftpPacketSize() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalPacketSize", int.class);
					m.setAccessible(true);
					m.invoke(getChannel(), (int)getConfiguration().getSftpPacketSize());
				}
				if(getConfiguration().getSftpWindowSize() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalWindowSize", int.class);
					m.setAccessible(true);
					m.invoke(getChannel(), (int)getConfiguration().getSftpWindowSize());
				}
				if(getConfiguration().getSftpWindowSizeMax() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalWindowSizeMax", int.class);
					m.setAccessible(true);
					m.invoke(getChannel(), (int)getConfiguration().getSftpWindowSizeMax());
				}
			}
			catch(Exception e) {
				SshConfiguration.getLogger().warn("Failed to set SFTP channel configuration via reflection.", e);
			}
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
