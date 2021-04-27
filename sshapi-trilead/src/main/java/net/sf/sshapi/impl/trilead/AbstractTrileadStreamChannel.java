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
package net.sf.sshapi.impl.trilead;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import com.trilead.ssh2.Session;
import com.trilead.ssh2.channel.Channel;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.SshChannelInputStream;
import net.sf.sshapi.util.Util;

abstract class AbstractTrileadStreamChannel<L extends SshChannelListener<C>, C extends SshExtendedChannel<L, C>>
		extends AbstractSshExtendedChannel<L, C> implements SshExtendedChannel<L, C> {
	private final Session session;
	private Field flagField;
	private Field channelField;
	private Field eofField;

	public AbstractTrileadStreamChannel(SshProvider provider, SshConfiguration configuration, Session channel) throws SshException {
		super(provider, configuration);
		this.session = channel;
	}

	@Override
	public boolean isOpen() {
		try {
			if (flagField == null) {
				/* Blimey. Thanks for making this so difficult Trilead */
				flagField = Session.class.getDeclaredField("flag_closed");
				flagField.setAccessible(true);
				channelField = Session.class.getDeclaredField("cn");
				channelField.setAccessible(true);
				eofField = Channel.class.getDeclaredField("EOF");
				eofField.setAccessible(true);
			}
			return super.isOpen() && !(Boolean) flagField.getBoolean(session) && !(Boolean) eofField.getBoolean(channelField.get(session));
		} catch (Exception e) {
			return false;
		}
	}

	public int exitCode() throws IOException {
		Integer t = session.getExitStatus();
		return t == null ? SshCommand.EXIT_CODE_NOT_RECEIVED : t.intValue();
	}

	public InputStream getInputStream() throws IOException {
		return new EventFiringInputStream(new SshChannelInputStream(session.getStdout(), this), SshDataListener.RECEIVED);
	}

	public OutputStream getOutputStream() throws IOException {
		return new EventFiringOutputStream(session.getStdin());
	}

	public InputStream getExtendedInputStream() throws IOException {
		return new EventFiringInputStream(new SshChannelInputStream(session.getStderr(), this), SshDataListener.EXTENDED);
	}

	protected Session getSession() {
		return session;
	}

	public void onCloseStream() throws SshException {
		session.close();
	}

	public final void onOpenStream() throws SshException {
		if (!Util.nullOrTrimmedBlank(configuration.getX11Host())) {
			try {
				session.requestX11Forwarding(configuration.getX11Host(), configuration.getX11Screen() + 6000, configuration.getX11Cookie(),
						configuration.isX11SingleConnection());
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
		onChannelOpen();
	}

	public abstract void onChannelOpen() throws SshException;
}
