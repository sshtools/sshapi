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
package net.sf.sshapi.impl.maverick;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.ssh.ChannelEventListener;
import com.sshtools.ssh.SshChannel;
import com.sshtools.ssh.SshSession;

import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedStreamChannel;

abstract class MaverickSshStreamChannel extends AbstractDataProducingComponent implements SshExtendedStreamChannel,
		ChannelEventListener {
	private final SshSession session;

	MaverickSshStreamChannel(SshSession session) {
		this.session = session;
	}

	public int exitCode() throws IOException {
		return session.exitCode();
	}

	public SshSession getMaverickSession() {
		return session;
	}

	public InputStream getInputStream() throws IOException {
		return session.getInputStream();
	}

	public InputStream getExtendedInputStream() throws IOException {
		return session.getStderrInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return session.getOutputStream();
	}

	protected SshChannel getChannel() {
		return session;
	}

	public void onClose() throws SshException {
		session.close();
	}

	protected final void onOpen() throws SshException {
		onChannelOpen();
		session.addChannelEventListener(this);
	}

	protected abstract void onChannelOpen() throws SshException;

	public void channelClosed(SshChannel channel) {
	}

	public void channelClosing(SshChannel channel) {
	}

	public void channelEOF(SshChannel channel) {
	}

	public void channelOpened(SshChannel channel) {
	}

	public void dataReceived(SshChannel channel, byte[] data, int off, int len) {
		fireData(SshDataListener.RECEIVED, data, off, len);
	}

	public void dataSent(SshChannel channel, byte[] data, int off, int len) {
		fireData(SshDataListener.SENT, data, off, len);
	}

	public void extendedDataReceived(SshChannel channel, byte[] data, int off, int len, int extendedDataType) {
		fireData(SshDataListener.EXTENDED, data, off, len);
	}
}
