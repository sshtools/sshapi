package com.maverick.ssh.tests.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.junit.After;
import org.junit.Before;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshException.Code;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.Logger.Level;
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
			SftpFile rpath = sftp.stat(resolveRemote(remotePath));
			SftpFile attr = rpath;
			assertTrue(rpath + " must exist", attr.isFile());
			assertEquals(rpath + " must have same size",
					localFile.length(), attr.getSize());
		}
	}

	protected SshFileTransferListener createProgress() {
		return new SshFileTransferListener() {
			private long total;
			
			@Override
			public void startedTransfer(String sourcePath, String targetPath, long length) {
				SshConfiguration.getLogger().log(Level.INFO, "Starting transfer of " + sourcePath + " to " + targetPath + " ("
						+ length + ")");
			}

			@Override
			public void transferProgress(String sourcePath, String targetPath, long progress) {
				total += progress;
			}

			@Override
			public void finishedTransfer(String sourcePath, String targetPath) {
				SshConfiguration.getLogger().log(Level.INFO, "Completed transfer of " + sourcePath + " to " + targetPath + ", " + total + "bytes");
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
