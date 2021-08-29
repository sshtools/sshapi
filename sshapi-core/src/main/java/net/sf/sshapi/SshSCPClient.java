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
 * The SCP client. Supports simple get/put operations.
 * 
 */
 public interface SshSCPClient extends SshFileTransferClient<SshLifecycleListener<SshSCPClient>, SshSCPClient> {
	/**
	 * Put a file or directory to a remote path.
	 * 
	 * @param remotePath path to copy file to.
	 * @param mode mode to create new files as
	 * @param sourceFile source file (or directory)
	 * @param recursive recursively copy this directory and all of it's children
	 *            to the remote path
	 * @throws SshException
	 */
	void put(String remotePath, String mode, File sourceFile, boolean recursive) throws SshException;

	/**
	 * Retrieve file(s) from a remote path, placing it in the destination file
	 * (or directory).
	 * 
	 * @param remoteFilePath remote file path
	 * @param destinationFile destination file or directory
	 * @param recursive recursively copy this path and all of it's children to
	 *            the destination directory
	 * @throws SshException
	 */
	void get(String remoteFilePath, File destinationFile, boolean recursive) throws SshException;
	
	/**
	 * Set whether mode, last access and last modified times will be preserved during transfers.
	 * 
	 * @param preserveTimes preserve times
	 */
	default void setPreserveAttributes(boolean preserveTimes) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Set whether mode, last access and last modified times will be preserved during transfers.
	 * 
	 * @param preserveTimes preserve times
	 */
	default boolean isPreserveAttributes() {
		throw new UnsupportedOperationException();
	}
}
