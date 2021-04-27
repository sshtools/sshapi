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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;

class OpenSshSCPClient extends AbstractSCPClient implements AbstractOpenSshClient {
	private ProcessBuilder pb;
	private Process process;
	private OpenSshClient client;
	private List<String> original;

	OpenSshSCPClient(ProcessBuilder pb, OpenSshClient client) {
		super(client.getProvider());
		this.pb = pb;
		this.client = client;
		original = new ArrayList<>(pb.command());
	}

	@Override
	protected void onClose() throws SshException {
		closeProcess();
	}

	@Override
	protected void onOpen() throws SshException {
	}

	@Override
	public void get(String remoteFilePath, File destinationFile, boolean recursive) throws SshException {
		try {
			synchronized (pb) {
				if (recursive)
					pb.command().add("-r");
				pb.command().add(client.getUsername() + "@" + client.getHostname() + ":" + remoteFilePath);
				pb.command().add(destinationFile.getAbsolutePath());
				process = client.setupAuthentication(pb).start();
				pump(process.getInputStream(), process.getErrorStream());
				int ex = process.waitFor();
				if (ex != 0)
					throw new SshException(SshException.GENERAL,
							String.format("scp command exited with non-zero status of %d", ex));
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		} catch (InterruptedException ioe) {
			throw new SshException(SshException.GENERAL, "Interrupted waiting for process to complete.", ioe);
		} finally {
			closeProcess();
		}
	}

	@Override
	protected void doPut(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		try {
			synchronized (pb) {
				if (recursive)
					pb.command().add("-r");
				pb.command().add(localfile.getAbsolutePath());
				pb.command().add(client.getUsername() + "@" + client.getHostname() + ":" + remotePath);
				process = client.setupAuthentication(pb).start();
				pump(process.getInputStream(), process.getErrorStream());
				int ex = process.waitFor();
				if (ex != 0)
					throw new SshException(SshException.GENERAL,
							String.format("scp command exited with non-zero status of %d", ex));
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		} catch (InterruptedException ioe) {
			throw new SshException(SshException.GENERAL, "Interrupted waiting for process to complete.", ioe);
		} finally {
			closeProcess();
		}
	}

	private void closeProcess() {
		if (process != null) {
			try {
				int result = process.waitFor();
				if (result != 0)
					SshConfiguration.getLogger().warn("Scp client exited with non-zero code {0}", result);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				synchronized (pb) {
					process = null;
					pb.command().clear();
					pb.command().addAll(original);
				}
			}
		}
	}
}
