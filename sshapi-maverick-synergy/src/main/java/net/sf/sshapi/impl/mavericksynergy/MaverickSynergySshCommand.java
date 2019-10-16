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
package net.sf.sshapi.impl.mavericksynergy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.client.SessionChannelNG;
import com.sshtools.client.SshClientContext;
import com.sshtools.common.shell.ShellPolicy;
import com.sshtools.common.ssh.Connection;

import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshException;

class MaverickSynergySshCommand extends AbstractDataProducingComponent<SshChannelListener<SshCommand>, SshCommand>
		implements SshCommand {
	private SessionChannelNG session;
	private InputStream extendedInputStream;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Connection<SshClientContext> con;
	private String command;

	MaverickSynergySshCommand(final Connection<SshClientContext> con, String command) {
		this.con = con;
		this.command = command;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return extendedInputStream;
	}

	@Override
	public int exitCode() throws IOException {
		return session.getExitCode();
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
	protected void onOpen() throws SshException {
		session = new SessionChannelNG(con, con.getContext().getPolicy(ShellPolicy.class).getSessionMaxPacketSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMaxWindowSize(),
				con.getContext().getPolicy(ShellPolicy.class).getSessionMinWindowSize());
		con.openChannel(session);
		if (!session.getOpenFuture().waitFor(30000).isSuccess()) {
			throw new IllegalStateException("Couldb not open session channel");
		}
		try {
			if (!session.executeCommand(command).waitFor(30000).isSuccess()) {
				throw new IllegalStateException("Could not execute command.");
			}
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException("Failed to execute command.", e);
		}
		inputStream = session.getInputStream();
		extendedInputStream = session.getErrorStream();
		outputStream = session.getOutputStream();
	}

	@Override
	protected void onClose() throws SshException {
		session.close();
	}
}
