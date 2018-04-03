package net.sf.sshapi.impl.maverick16;

import java.io.File;

import net.sf.sshapi.AbstractSCPClient;

import com.maverick.sftp.FileTransferProgress;
import com.sshtools.scp.ScpClient;

class MaverickSCPClient extends AbstractSCPClient implements FileTransferProgress {

	/**
	 * 
	 */
	private final MaverickSshClient maverickSshClient;

	/**
	 * @param maverickSshClient
	 */
	MaverickSCPClient(MaverickSshClient maverickSshClient) {
		this.maverickSshClient = maverickSshClient;
	}

	private ScpClient scpClient;
	private String path;
	private String targetPath;
	private long lastProgress;

	protected void onClose() throws net.sf.sshapi.SshException {
		try {
			scpClient.exit();
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}
		super.onClose();
	}

	protected void onOpen() throws net.sf.sshapi.SshException {
		try {
			scpClient = new ScpClient(new File(System.getProperty("user.dir")), this.maverickSshClient.client);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.GENERAL, e);
		}
		super.onOpen();

	}

	public void get(String remoteFilePath, File destinationFile, boolean recursive) throws net.sf.sshapi.SshException {
		try {
			scpClient.get(destinationFile.getAbsolutePath(), remoteFilePath, recursive, this);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	public void doPut(String remotePath, String mode, File sourceFile, boolean recursive) throws net.sf.sshapi.SshException {
		try {
			targetPath = remotePath;
			scpClient.put(sourceFile.getAbsolutePath(), remotePath, recursive, this);
		} catch (Exception e) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.IO_ERROR, e);
		}
	}

	public void completed() {
		fireFileTransferFinished(path, targetPath);

	}

	public boolean isCancelled() {
		return false;
	}

	public void progressed(long arg0) {		
		fireFileTransferProgressed(path, targetPath, arg0 - lastProgress);
		lastProgress = arg0;
	}

	public void started(long arg0, String arg1) {
		path = arg1;
		lastProgress = 0;
		fireFileTransferStarted(arg1, targetPath, arg0);
	}

}