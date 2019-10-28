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
import java.io.OutputStream;
import java.util.Objects;

import com.jcraft.jsch.Channel;

import net.sf.sshapi.AbstractSshStreamChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshInput;
import net.sf.sshapi.SshProvider;

abstract class AbstractJschStreamChannel<L extends SshChannelListener<C>, C extends SshExtendedChannel<L, C>>
		extends AbstractSshStreamChannel<L, C> implements SshExtendedChannel<L, C> {
	private final Channel channel;

	private InputStream in;
	private OutputStream out;
	private InputStream ext;
	private SshInput errInput;
	private Thread errThread;

	public AbstractJschStreamChannel(SshProvider provider, SshConfiguration configuration, Channel channel) throws SshException {
		super(provider, configuration);
		this.channel = channel;
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

	@Override
	public int exitCode() throws IOException {
		return channel.getExitStatus();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return ext;
	}

	protected Channel getChannel() {
		return channel;
	}

	@Override
	public void onClose() throws SshException {
		channel.disconnect();
	}

	@Override
	public void onOpen() throws SshException {
		try {
			in = channel.getInputStream();
			out = channel.getOutputStream();
			ext = channel.getExtInputStream();
			channel.connect(Integer.parseInt(
					configuration.getProperties().getProperty(JschSshProvider.CFG_CHANNEL_CONNECT_TIMEOUT, "3000")));
		} catch (Exception e) {
			throw new SshException("Failed to connect channel.", e);
		}
		onChannelOpen();
	}

	protected SshConfiguration getConfiguration() {
		return configuration;
	}

	protected abstract void onChannelOpen() throws SshException;

	protected abstract void onChannelClose() throws SshException;
}
