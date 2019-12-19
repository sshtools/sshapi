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
import java.io.OutputStream;

import com.maverick.ssh.ChannelEventListener;
import com.maverick.ssh.SshChannel;
import com.maverick.ssh.SshSession;
import com.maverick.ssh2.Ssh2Session;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.SshChannelInputStream;

abstract class AbstractMaverickSshStreamChannel<L extends SshChannelListener<C>, C extends SshExtendedChannel<L, C>>
		extends AbstractSshExtendedChannel<L, C> implements SshExtendedChannel<L, C>, ChannelEventListener {
	private final com.maverick.ssh.SshSession session;

	AbstractMaverickSshStreamChannel(SshProvider provider, SshConfiguration configuration,
			com.maverick.ssh.SshSession session) {
		super(provider, configuration);
		this.session = session;
		setFiresOwnCloseEvents(true);
	}

	@Override
	public void sendSignal(Signal signal) throws SshException {
		if (session instanceof Ssh2Session) {
			try {
				((Ssh2Session) session).signal(signal.name());
			} catch (com.maverick.ssh.SshException e) {
				throw new SshException(SshException.GENERAL, e);
			}
		} else
			super.sendSignal(signal);
	}

	@Override
	public final boolean isOpen() {
		return super.isOpen() && !session.isClosed();
	}

	@Override
	public int exitCode() throws IOException {
		return session.exitCode();
	}

	public SshSession getMaverickSession() {
		return session;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new SshChannelInputStream(session.getInputStream(), this);
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return new SshChannelInputStream(session.getStderrInputStream(), this);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return session.getOutputStream();
	}

	protected com.maverick.ssh.SshChannel getChannel() {
		return session;
	}

	@Override
	public void onCloseStream() throws SshException {
		session.close();
	}

	@Override
	protected final void onOpenStream() throws SshException {
		onChannelOpen();
		session.addChannelEventListener(this);
	}

	protected abstract void onChannelOpen() throws SshException;

	@Override
	public void channelClosed(SshChannel channel) {
		fireClosed();
	}

	@Override
	public void channelClosing(SshChannel channel) {
		fireClosing();
	}

	@Override
	public void channelEOF(SshChannel channel) {
		fireEof();
	}

	@Override
	public void channelOpened(SshChannel channel) {
	}

	@Override
	public void dataReceived(SshChannel channel, byte[] data, int off, int len) {
		fireData(SshDataListener.RECEIVED, data, off, len);
	}

	@Override
	public void dataSent(SshChannel channel, byte[] data, int off, int len) {
		fireData(SshDataListener.SENT, data, off, len);
	}

	@Override
	public void extendedDataReceived(SshChannel channel, byte[] data, int off, int len, int extendedDataType) {
		fireData(SshDataListener.EXTENDED, data, off, len);
	}
}
