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
package net.sf.sshapi.impl.maverick;

import java.io.File;

import com.sshtools.scp.ScpClient;
import com.sshtools.sftp.FileTransferProgress;

import net.sf.sshapi.AbstractSCPClient;

class MaverickSCPClient extends AbstractSCPClient implements FileTransferProgress {

	/**
	 * 
	 */
	private final MaverickSshClient maverickSshClient;

	/**
	 * @param maverickSshClient
	 */
	MaverickSCPClient(MaverickSshClient maverickSshClient) {
		super(maverickSshClient.getProvider());
		this.maverickSshClient = maverickSshClient;
	}

	private ScpClient scpClient;
	private String path;
	private String targetPath;
	private long lastProgress;

	@Override
	protected void onClose() throws net.sf.sshapi.SshException {
		try {
			scpClient.exit();
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}
		super.onClose();
	}

	@Override
	protected void onOpen() throws net.sf.sshapi.SshException {
		try {
			scpClient = new ScpClient(new File(System.getProperty("user.dir")), this.maverickSshClient.client);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}
		super.onOpen();

	}

	@Override
	public void get(String remoteFilePath, File destinationFile, boolean recursive) throws net.sf.sshapi.SshException {
		try {
			scpClient.get(destinationFile.getAbsolutePath(), remoteFilePath, recursive, this);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	@Override
	public void doPut(String remotePath, String mode, File sourceFile, boolean recursive) throws net.sf.sshapi.SshException {
		try {
			targetPath = remotePath;
			scpClient.put(sourceFile.getAbsolutePath(), remotePath, recursive, this);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	@Override
	public void completed() {
		fireFileTransferFinished(path, targetPath);

	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void progressed(long arg0) {		
		fireFileTransferProgressed(path, targetPath, arg0 - lastProgress);
		lastProgress = arg0;
	}

	@Override
	public void started(long arg0, String arg1) {
		path = arg1;
		lastProgress = 0;
		fireFileTransferStarted(arg1, targetPath, arg0);
	}

}