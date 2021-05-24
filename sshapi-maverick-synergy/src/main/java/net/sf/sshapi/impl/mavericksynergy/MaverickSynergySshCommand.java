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
package net.sf.sshapi.impl.mavericksynergy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.client.AbstractSessionChannel;
import com.sshtools.client.PseudoTerminalModes;
import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClientContext;
import com.sshtools.common.shell.ShellPolicy;
import com.sshtools.synergy.ssh.Connection;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;

class MaverickSynergySshCommand extends AbstractSshExtendedChannel<SshChannelListener<SshCommand>, SshCommand>
		implements SshCommand {
	private SessionChannelNG session;
	private InputStream extendedInputStream;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Connection<SshClientContext> con;
	private String command;
	private int cols;
	private int rows;
	private int pixWidth;
	private int pixHeight;
	private byte[] terminalModes;
	private String termType;

	MaverickSynergySshCommand(SshProvider provider, SshConfiguration configuration,
			final Connection<SshClientContext> con, String command, String termType, int cols, int rows, int pixWidth,
			int pixHeight, byte[] terminalModes) {
		super(provider, configuration);
		this.con = con;
		this.command = command;
		this.termType = termType;
		this.terminalModes = terminalModes;
		this.cols = cols;
		this.rows = rows;
		this.pixWidth = pixWidth;
		this.pixHeight = pixHeight;
		setFiresOwnCloseEvents(true);
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return extendedInputStream;
	}

	@Override
	public int exitCode() throws IOException {
		int exitCode = session.getExitCode();
		return exitCode == AbstractSessionChannel.EXITCODE_NOT_RECEIVED ? SshCommand.EXIT_CODE_NOT_RECEIVED : exitCode;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return outputStream;
	}

	@Override
	protected final void onOpenStream() throws SshException {
		session = new SessionChannelNG(con.getContext().getPolicy(ShellPolicy.class).getSessionMaxPacketSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMinWindowSize()) {

			@Override
			protected void onChannelClosed() {
				fireClosed();
			}

			@Override
			protected void onChannelClosing() {
				fireClosing();
			}

			@Override
			protected void onRemoteEOF() {
				fireEof();
			}

		};
		con.openChannel(session);
		if (termType != null) {
			PseudoTerminalModes ptm = new PseudoTerminalModes();
			if (terminalModes != null) {
				try {
					for (int i = 0; i < terminalModes.length; i++) {
						ptm.setTerminalMode(terminalModes[i], true);
					}
				} catch (com.sshtools.common.ssh.SshException e) {
					throw new SshException("Failed to set terminal modes.", e);
				}
			}
			session.allocatePseudoTerminal(termType, cols, rows, pixWidth, pixHeight, ptm);
		}
		if (!session.getOpenFuture().waitFor(30000).isSuccess()) {
			throw new IllegalStateException("Couldb not open session channel");
		}
		try {
			session.executeCommand(command).waitForever();
//			if (!session.executeCommand(command).waitFor(30000).isSuccess()) {
//				throw new IllegalStateException("Could not execute command.");
//			}
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException("Failed to execute command.", e);
		}
		inputStream = new EventFiringInputStream(session.getInputStream(), SshDataListener.RECEIVED);
		extendedInputStream = new EventFiringInputStream(session.getStderrStream(), SshDataListener.EXTENDED);
		outputStream = new EventFiringOutputStream(session.getOutputStream());
	}

	@Override
	protected void onCloseStream() throws SshException {
		session.close();
	}
}
