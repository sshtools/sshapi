package com.maverick.ssh.tests.client;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.maverick.ssh.tests.RandomFilesGenerator;

import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpException;

public abstract class AbstractClientFiles extends AbstractClientConnected {
	private static final String TEST_DIRECTORY_NAME = "testdirectory";
	public static final int TEST_DIRECTORY_PERMISSIONS = 0644;
	public static final int TEST_FILE_PERMISSIONS = 0644;
	protected static RandomFilesGenerator randomFiles;
	protected static RandomFilesGenerator randomMultilevelFiles;
	protected String cwd;
	protected String lwd;

	@BeforeClass
	public static void setUpBeforeClassFiles() throws Exception {
		randomFiles = new RandomFilesGenerator();
		randomMultilevelFiles = new RandomFilesGenerator(true, "sftp-integration-tests-multilevel");
	}

	protected String resolveRemote(String path) {
		return cwd + (path.equals("/") ? "" : "/" + path);
	}

	protected String resolveLocal(String path) {
		return lwd + (path.equals("/") ? "" : "/" + path);
	}

	@Before
	public void onFilesSetUp() throws Exception {
		randomFiles.resetLocal();
		lwd = randomFiles.getLocalFilesDir().getAbsolutePath();
		String name = ssh.getProvider().getClass().getName();
		if (name.equals("net.sf.sshapi.impl.j2ssh.J2SshProvider")) {
			/*
			 * J2SSH has a bug that means it fails to delete broken symlinks. This cannot be
			 * worked around in the provider, so we use a different test directory name each
			 * time.
			 * 
			 * There will need to be an external clean up procedure before tests are run
			 */
			cwd = TEST_DIRECTORY_NAME + "-" + UUID.randomUUID().toString();
		} else {
			cwd = TEST_DIRECTORY_NAME;
			// We do all our work in a single directory to make initial clean up
			// easier
			timeout(() -> {
				try (SftpClient sftp = ssh.sftp()) {
					try {
						sftp.rm(cwd, true);
					} catch (SftpException sftpe) {
						if (sftpe.getCode() != SftpException.SSH_FX_NO_SUCH_FILE) {
							throw sftpe;
						}
					}
					sftp.mkdir(cwd);
				}
				return null;
			}, 10000);
		}
	}

	@After
	public final void onFilesTearDown() throws Exception {
		randomFiles.cleanup();
	}
}
