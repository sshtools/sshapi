package net.sf.sshapi;

import java.io.File;

/**
 * Abstract implementation of an SCP client. This adds checking of the
 * parameters passed to {@link #put(String, String, File, boolean)}.
 */
public abstract class AbstractSCPClient extends
		AbstractFileTransferClient<SshLifecycleListener<SshSCPClient>, SshSCPClient> implements SshSCPClient {

	protected void onOpen() throws SshException {
	}

	protected void onClose() throws SshException {
	}

	public final void put(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		if (!localfile.exists()) {
			throw new SshException(SshException.IO_ERROR, localfile + " does not exist");
		}
		if (!localfile.isFile() && !localfile.isDirectory()) {
			throw new SshException(SshException.IO_ERROR, localfile + " is not a regular file or directory");
		}
		if (localfile.isDirectory() && !recursive) {
			throw new SshException(SshException.IO_ERROR,
					localfile + " is a directory, so recursive mode must be used.");
		}
		if ((remotePath == null) || remotePath.equals("")) {
			remotePath = ".";
		}
		// else {
		// if (localfile.isDirectory() &&
		// !Util.basename(remotePath).equals(localfile.getName())) {
		// remotePath += "/" + localfile.getName();
		// }
		// }
		doPut(remotePath, mode, localfile, recursive);
	}

	/**
	 * Sub-classes should implement this to perform the actual upload of the
	 * file(s).
	 * 
	 * @param remotePath
	 *            remote path to upload files to
	 * @param mode
	 *            mode string
	 * @param localfile
	 *            local file to copy
	 * @param recursive
	 *            recursive
	 * @throws SshException
	 *             on any error
	 */
	protected abstract void doPut(String remotePath, String mode, File localfile, boolean recursive)
			throws SshException;
}
