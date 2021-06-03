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
package com.maverick.ssh.tests.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.After;
import org.junit.Before;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshException.Code;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;

public abstract class AbstractClientSftp extends AbstractClientFiles {

	
	protected SftpClient sftp;

	@Before
	public final void onConnectedSetUp() throws Exception {
		sftp = ssh.sftp();
	}

	@After
	public final void onConnectedTearDown() throws Exception {
		if (sftp != null) {
			sftp.close();
		}
	}
	
	protected void compareDirs(File localFile, String remotePath)
			throws SftpException, SshException {
		if (localFile.isDirectory()) {
			for (File f : localFile.listFiles()) {
				compareDirs(f, remotePath + "/" + f.getName());
			}
		} else {
			String resolvedPath = resolveRemote(remotePath);
			System.out.println("Local file: " + localFile + " : " + remotePath + " resolved " + resolvedPath);
			assertTrue(resolvedPath + " must exist", SftpFile.exists(sftp, resolvedPath));
			assertEquals(resolvedPath + " must have same size",
					localFile.length(), sftp.stat(resolvedPath).getSize());
		}
	}

	protected SshFileTransferListener createProgress() {
		return new SshFileTransferListener() {
			private long total;
			
			@Override
			public void startedTransfer(String sourcePath, String targetPath, long length) {
				LOG.info("Starting transfer of {0} to {1} ({2})", sourcePath, targetPath, length);
			}

			@Override
			public void transferProgress(String sourcePath, String targetPath, long progress) {
				total += progress;
			}

			@Override
			public void finishedTransfer(String sourcePath, String targetPath) {
				LOG.info("Completed transfer of {0} to {1}, {2} bytes", sourcePath, targetPath, total);
			}

		};
	}

	protected void createFile(String path) throws SftpException,
			SshException {
		sftp.put(resolveRemote(path), new ByteArrayInputStream("Some stuff\n".getBytes()), TEST_FILE_PERMISSIONS);
	}

	protected static void assertSftpStatus(SftpException sftpe, Code code)
			throws SftpException {
		assertSftpStatus("SftpStatusException should have code", sftpe, code);
	}

	protected static void assertSftpStatus(String message,
			SftpException sftpe, Code code) throws SftpException {
		assertEquals(message, code, sftpe.getCode());
		throw sftpe;
	}
}
