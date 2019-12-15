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
import java.io.OutputStream;
import java.util.Objects;

import com.sshtools.j2ssh.session.SessionChannelClient;

import net.sf.sshapi.AbstractSshStreamChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshInput;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.Util;

abstract class AbstractJ2SshStreamChannel<L extends SshChannelListener<C>, C extends SshExtendedChannel<L, C>>
		extends AbstractSshStreamChannel<L, C> implements SshExtendedChannel<L, C> {
	
	private final SessionChannelClient channel;
	private final SshConfiguration configuration;
	private SshInput errInput;
	private Thread errThread;

	public AbstractJ2SshStreamChannel(SshProvider provider, SshConfiguration configuration, SessionChannelClient channel) {
		super(provider, configuration);
		this.channel = channel;
		this.configuration = configuration;
	}

	@Override
	public void setErrInput(SshInput errInput) {
		if (!Objects.equals(errInput, this.errInput)) {
			this.errInput = errInput;
			if (errInput == null) {
				errThread.interrupt();
			} else {
				try {
					errThread = pump(errInput, getExtendedInputStream());
				} catch (IOException e) {
					throw new IllegalStateException("Failed to extended input stream.", e);
				}
			}
		}
	}

	public int exitCode() throws IOException {
		Integer i = channel.getExitCode();
		return i == null ? -1 : i.intValue();
	}

	public InputStream getInputStream() throws IOException {
		return channel.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return channel.getOutputStream();
	}

	public InputStream getExtendedInputStream() throws IOException {
		return channel.getStderrInputStream();
	}

	protected SessionChannelClient getChannel() {
		return channel;
	}

	public final void onOpen() throws SshException {
		if (configuration.getX11Cookie() != null) {
			byte[] x11Cookie = configuration.getX11Cookie();
			String hexCookie = Util.formatAsHexString(x11Cookie);
			int x11Port = configuration.getX11Port();
			try {
				channel.requestX11Forwarding(x11Port - 6000, hexCookie);
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
		onChannelOpen();
	}

	public void onCloseStream() throws SshException {
		try {
			channel.close();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	protected abstract void onChannelOpen() throws SshException;
}
