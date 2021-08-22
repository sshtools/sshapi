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
package net.sf.sshapi.impl.openssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshCommandListener;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;

class OpenSshCommand extends AbstractSshExtendedChannel<SshCommandListener, SshCommand> implements SshCommand {
	private ProcessBuilder pb;
	private Process process;
	private final String termType;
	private String command;
	private int exitVal = Integer.MIN_VALUE;
	private OpenSshClient client;

	OpenSshCommand(OpenSshClient client, ProcessBuilder pb, String termType, String command) {
		super(client.getProvider(), client.getConfiguration());
		this.pb = pb;
		this.termType = termType;
		this.command = command;
		this.client = client;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		if (process == null)
			throw new IllegalStateException("This command has either completed or has not been started.");
		return new EventFiringInputStream(process.getErrorStream(), SshDataListener.EXTENDED);
	}

	@Override
	public int exitCode() throws IOException {
		if (exitVal == Integer.MIN_VALUE)
			return SshCommand.EXIT_CODE_NOT_RECEIVED;
		return exitVal;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (process == null)
			throw new IllegalStateException("This command has either completed or has not been started.");
		return new EventFiringInputStream(process.getInputStream(), SshDataListener.RECEIVED);
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (process == null)
			throw new IllegalStateException("This command has either completed or has not been started.");
		return new EventFiringOutputStream(process.getOutputStream());
	}

	@Override
	protected void onCloseStream() throws SshException {
		try {
			if(process != null) {
				process.destroy();
				exitVal = process.waitFor();
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			process = null;
		}
	}

	@Override
	protected void onOpenStream() throws SshException {
		try {
			if (termType != null && termType.length() > 0) {
				pb.command().add(0, "unbuffer");
				pb.command().add(1, "-p");
				pb.environment().put("TERM", termType);
				pb.command().add(3, "-t");
			} else
				pb.command().add(3, "-T");
			pb.command().add(OpenSsh.escape(command));
			process = client.setupAuthentication(pb).start();
			try {
				Thread.sleep(3000);
				
				/* NOTE: Not perfect. If the command ran returns either 127 or 5 they
				 *       will be interpreted as authentication failure or command not found.
				 *       Anything else will assume there is stream data, but the exit value
				 *       be obtained after the close().
				 */
				
				if (process.exitValue() == 5) {
					/* Authentication failure */
					exitVal = process.exitValue();
					process = null;
					throw new SshException(SshException.AUTHENTICATION_FAILED);
				}
				else if (process.exitValue() == 127) {
					/* Command not found */
					exitVal = process.exitValue();
					process = null;
				}
				else if (process.exitValue() != 0) {
					
				}
			} catch (IllegalThreadStateException | InterruptedException e) {
			}
		} catch (SshException e) {
			throw e;
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		}
	}
}
