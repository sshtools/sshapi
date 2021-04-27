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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.trilead.ssh2.SCPClient;

import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.util.Util;

final class TriliadSCPClient extends AbstractSCPClient implements SshSCPClient {

	/**
	 * 
	 */
	private final TrileadSshClient ganymedSshClient;

	/**
	 * @param ganymedSshClient
	 */
	TriliadSCPClient(TrileadSshClient ganymedSshClient) {
		super(ganymedSshClient.getProvider());
		this.ganymedSshClient = ganymedSshClient;
	}

	private SCPClient client;

	protected void onClose() throws SshException {
		/*
		 * Ganymed opens and closes sessions itself when file operations are
		 * performed
		 */
	}

	protected void onOpen() throws SshException {
		try {
			client = this.ganymedSshClient.connection.createSCPClient();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	protected void doPut(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		try {
			if (localfile.isDirectory()) {
				File[] f = localfile.listFiles();
				if (f == null) {
					throw new IOException("Could not list local directory " + localfile + ".");
				}
				for (int i = 0; i < f.length; i++) {
					if (recursive || f[i].isFile()) {
						doPut(remotePath + "/" + f[i].getName(), mode, f[i], recursive);
					}
				}
			} else {
				fireFileTransferStarted(localfile.getPath(), remotePath, localfile.length());
				String remoteDir = Util.dirname(remotePath);
				String remoteFile = Util.basename(remotePath);
				try {
					client.put(localfile.getPath(), remoteFile, remoteDir, mode == null ? "0600" : mode);
				} finally {
					fireFileTransferFinished(localfile.getPath(), remotePath);
				}
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	public void get(final String remotePath, File targetFile, boolean recursive)
			throws SshException {
		if (recursive) {
			throw new UnsupportedOperationException(
					"Trilead does not support recursively retrieving files from the server using SCP");
		}
		try {
			if (targetFile.isDirectory()) {
				FileOutputStream fout = new FileOutputStream(new File(targetFile, Util.basename(remotePath)));
				try {
					client.get(remotePath, fout);
				}
				finally {
					fout.close();
				}
			} else {
				FileOutputStream fout = new FileOutputStream(targetFile);
				try {
					client.get(remotePath, fout);
				}
				finally {
					fout.close();
				}
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int len;
		while ((len = in.read(buffer)) != -1) {
		    out.write(buffer, 0, len);
		}
	}
}