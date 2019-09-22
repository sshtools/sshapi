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
package net.sf.sshapi.impl.maverickng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import com.sshtools.client.PseudoTerminalModes;
import com.sshtools.client.SessionChannel;
import com.sshtools.client.SshClientContext;
import com.sshtools.client.tasks.ShellTask;
import com.sshtools.common.ssh.Connection;

import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

class MaverickNGSshShell extends AbstractDataProducingComponent<SshChannelListener<SshShell>, SshShell>
		implements SshShell {

	private Semaphore sem;
	private SessionChannel session;
	private InputStream extendedInputStream;
	private InputStream inputStream;
	private OutputStream outputStream;
	private Connection<SshClientContext> con;
	private String termType;
	private int cols;
	private int rows;
	private int pixWidth;
	private int pixHeight;

	MaverickNGSshShell(final Connection<SshClientContext> con, String termType, int cols, int rows, int pixWidth,
			int pixHeight, byte[] terminalModes) {
		this.termType = termType;
		this.cols = cols;
		this.rows = rows;
		this.pixWidth = pixWidth;
		this.pixHeight = pixHeight;
		this.con = con;
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
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		if (termType == null)
			throw new IllegalStateException("Not a pseudo tty.");
		session.changeTerminalDimensions(width, height, pixw, pixh);
	}

	@Override
	protected void onOpen() throws SshException {
		sem = new Semaphore(1);
		try {
			sem.acquire();
			con.addTask(new ShellTask(con) {
				private boolean released;

				@Override
				protected void onOpenSession(SessionChannel session) {
					MaverickNGSshShell.this.session = session;
					if (termType != null) {
						session.allocatePseudoTerminal(termType, cols, rows, pixWidth, pixHeight,
								new PseudoTerminalModes());
					}
					session.startShell();
					release();
				}

				private void release() {
					if (!released) {
						sem.release();
						released = true;
					}
				}

				@Override
				protected void onCloseSession(SessionChannel session) {
					release();
				}
			});
			sem.acquire();
			sem.release();
			
			inputStream = session.getInputStream();
			extendedInputStream = session.getStderrInputStream();
			outputStream = session.getOutputStream();
		} catch (InterruptedException ie) {
			throw new SshException(SshException.GENERAL, ie);
		} 

	}

	@Override
	protected void onClose() throws SshException {
		session.close();
	}
}
