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
package net.sf.sshapi.impl.ganymed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.channel.Channel;
import net.sf.sshapi.AbstractSshStreamChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshInput;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.SshChannelInputStream;
import net.sf.sshapi.util.Util;

abstract class AbstractGanymedStreamChannel<L extends SshChannelListener<C>, C extends SshExtendedChannel<L, C>>
		extends AbstractSshStreamChannel<L, C> implements SshExtendedChannel<L, C> {

	private SshInput errInput;
	private Thread errThread;
	private final Session session;

	public AbstractGanymedStreamChannel(SshProvider provider, SshConfiguration configuration, Session channel) throws SshException {
		super(provider, configuration);
		this.session = channel;
	}

	@Override
	public void sendSignal(Signal signal) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen() {
		return super.isOpen() && session.getState() == Channel.STATE_OPEN;
	}

	@Override
	public int exitCode() throws IOException {
		Integer t = session.getExitStatus();
		return t == null ? SshCommand.EXIT_CODE_NOT_RECEIVED : t.intValue();
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return new EventFiringInputStream(new SshChannelInputStream(session.getStderr(), this), SshDataListener.EXTENDED) {
			@Override
			protected void fireEof() {
				AbstractGanymedStreamChannel.this.fireEof();
			}
		};
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new EventFiringInputStream(new SshChannelInputStream(session.getStdout(), this), SshDataListener.RECEIVED) {
			@Override
			protected void fireEof() {
				AbstractGanymedStreamChannel.this.fireEof();
			}
		};
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new EventFiringOutputStream(session.getStdin());
	}

	public abstract void onChannelOpen() throws SshException;

	@Override
	public void onCloseStream() throws SshException {
		session.close();
	}
	
	protected void beforeClose() throws SshException {
		fireEof();
	}
	
	@Override
	public final void onOpenStream() throws SshException {
		if (!Util.nullOrTrimmedBlank(configuration.getX11Host())) {
			try {
				session.requestX11Forwarding(configuration.getX11Host(), configuration.getX11Screen() + 6000,
						configuration.getX11Cookie(), configuration.isX11SingleConnection());
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
		onChannelOpen();
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

	protected Session getSession() {
		return session;
	}

}
