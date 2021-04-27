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
package net.sf.sshapi.impl.sshj;

import java.io.File;
import java.io.IOException;

import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPDownloadClient;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;
import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.SshException;

public class SSHJSCPClient extends AbstractSCPClient {

	private SCPFileTransfer scp;

	public SSHJSCPClient(SSHJSshClient client) {
		super(client.getProvider());
		scp = client.getSsh().newSCPFileTransfer();
	}

	@Override
	public void get(String remoteFilePath, File destinationFile, boolean recursive) throws SshException {
		try {
			SCPDownloadClient dl = scp.newSCPDownloadClient();
			dl.setRecursiveMode(recursive);
			dl.copy(remoteFilePath, new FileSystemFile(destinationFile));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to download %s to %s.", remoteFilePath, destinationFile), e);
		}
	}

	@Override
	protected void doPut(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		try {
			SCPUploadClient dl = scp.newSCPUploadClient();
			dl.copy(new FileSystemFile(localfile), remotePath);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to upload %s to %s.", localfile, remotePath), e);
		}

	}

}
