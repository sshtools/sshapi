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
package net.sf.sshapi;

import java.io.File;

/**
 * Abstract implementation of an SCP client. This adds checking of the
 * parameters passed to {@link #put(String, String, File, boolean)}.
 */
public abstract class AbstractSCPClient extends
		AbstractFileTransferClient<SshLifecycleListener<SshSCPClient>, SshSCPClient> implements SshSCPClient {
	
	protected AbstractSCPClient(SshProvider provider) {
		super(provider);
	}

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
