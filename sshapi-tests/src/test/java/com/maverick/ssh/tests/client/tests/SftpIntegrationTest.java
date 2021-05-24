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
package com.maverick.ssh.tests.client.tests;

import static com.maverick.ssh.tests.Size.size;
import static com.maverick.ssh.tests.Util.compare;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.RandomizedInputStream;
import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.Size;
import com.maverick.ssh.tests.Wrap;
import com.maverick.ssh.tests.client.AbstractClientSftp;
import com.maverick.ssh.tests.client.CountingFileTransferProgress;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.sftp.SftpClient.EOLPolicy;
import net.sf.sshapi.sftp.SftpClient.TransferMode;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpFileVisitor;
import net.sf.sshapi.sftp.SftpOperation;

public class SftpIntegrationTest extends AbstractClientSftp {
	//
	// Mkdir
	//
	@Test
	public void testMkdir() throws Exception {
		timeout(() -> {
			sftp.mkdir(resolveRemote("dir1"));
			assertTrue("Newly created directory should exist and be a directory",
					sftp.stat(resolveRemote("dir1")).isDirectory());
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	@Test
	public void testMkdirs() throws Exception {
		timeout(() -> {
			sftp.mkdirs(resolveRemote("dir2/dir3"));
			assertTrue("Newly created directory should exist and be a directory",
					sftp.stat(resolveRemote("dir2/dir3")).isDirectory());
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	@Test(expected = SftpException.class)
	public void testMkdirExisting() throws Exception {
		testMkdir();
		timeout(() -> {
			sftp.mkdir(resolveRemote("dir1"));
			return null;
		}, TimeUnit.MINUTES.toMillis(3));
	}

	@Test(expected = SftpException.class)
	public void testMkdirNoPath() throws Exception {
		timeout(() -> {
			try {
				sftp.mkdir(resolveRemote("XXXXXX/dir1"));
			} catch (SftpException sftpe) {
				assertSftpStatus(sftpe, SftpException.SSH_FX_NO_SUCH_FILE);
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	//
	// stat (permissions etc)
	//
	@Test
	public void testChgrp() throws Exception {
		timeout(() -> {
			assumeTrue("Must be running as administrator", isAdministrator());
			assertServerCapabilities(ServerCapability.SUPPORTS_GROUPS);
			createFile("testChgrpFile");
			sftp.chgrp(resolveRemote("testChgrpFile"), config.getAlternateGid());
			assertEquals("Gid should now be the alternate", config.getAlternateGid(),
					Integer.valueOf(sftp.stat(resolveRemote("testChgrpFile")).getGID()).intValue());
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	@Test
	public void testLastModified() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support last modified times.",
					ssh.getProvider().getCapabilities().contains(Capability.SET_LAST_MODIFIED));
			createFile("testLastModFile");
			long lastMod = sftp.stat(resolveRemote("testLastModFile")).getLastModified();
			assertNotEquals("Initial last modified times should not be zero", 0, lastMod);
			Thread.sleep(2000);
			OutputStream out = sftp.put(resolveRemote("testLastModFile"), TEST_FILE_PERMISSIONS);
			try {
				out.write("Stuff\n".getBytes());
				out.flush();
			} finally {
				out.close();
			}
			long newLastMod = sftp.stat(resolveRemote("testLastModFile")).getLastModified();
			assertNotEquals("Last modified times should be different", lastMod, newLastMod);
			assertTrue(String.format("Last modified should differ by only a few seconds. Difference of %d",
					newLastMod - lastMod), Math.abs(newLastMod - lastMod) < 6000);
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	@Test
	public void testChmodReadOnlyFile() throws Exception {
		chmodTest("testChmodReadOnlyFile", 128, false);
	}

	@Test
	public void testChmodExecuteFile() throws Exception {
		chmodTest("testChmodExecuteFile", 64, true);
	}

	void chmodTest(String name, int mask, boolean set) throws Exception {
		timeout(() -> {
			assertServerCapabilities(ServerCapability.SUPPORTS_PERMISSIONS);
			createFile(name);
			int permissions = sftp.stat(resolveRemote(name)).getPermissions();
			int originalPerms = permissions;
			if (set) {
				assertTrue(String.format("Should be unset by default. Permissions %d, Mask %d", permissions, mask),
						(permissions & mask) == 0);
				sftp.chmod(resolveRemote(name), originalPerms |= mask);
				permissions = sftp.stat(resolveRemote(name)).getPermissions();
				assertTrue("Should be set", (permissions & mask) > 0);
			} else {
				assertTrue("Should be unset by default", (permissions & mask) > 0);
				sftp.chmod(resolveRemote(name), originalPerms &= ~(mask));
				permissions = sftp.stat(resolveRemote(name)).getPermissions();
				assertTrue("Should now be unset", (permissions & mask) == 0);
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(30));
	}

	//
	// Copy files
	//
	@Test
	public void testCopyToRemoteRecursively() throws Exception {
		timeout(() -> {
			File testFilesDir = randomMultilevelFiles.getTestFilesDir();
			runAndWatchProgress(
					() -> sftp.upload(testFilesDir, resolveRemote(testFilesDir.getName()), true, false, true),
					createProgress());
			compareDirs(testFilesDir, testFilesDir.getName());
			return null;
		}, TimeUnit.MINUTES.toMillis(5));
	}

	@Test
	public void testCopyToRemoteSynchronize() throws Exception {
		timeout(() -> {
			// Initial copy
			File testFilesDir = randomMultilevelFiles.getTestFilesDir();
			File newTestDir = new File(testFilesDir.getParentFile(), testFilesDir.getName() + ".tmp");
			try {
				FileUtils.copyDirectory(testFilesDir, newTestDir);
				List<String> allPaths = new ArrayList<>();
				for (Iterator<File> fit = FileUtils.iterateFiles(newTestDir, TrueFileFilter.TRUE,
						TrueFileFilter.TRUE); fit.hasNext();) {
					File file = fit.next();
					if (file.isFile())
						allPaths.add(file.getPath());
				}
				SftpOperation op = runAndWatchProgress(
						() -> sftp.upload(newTestDir, resolveRemote(newTestDir.getName()), true, false, true),
						createProgress());

				// All: 72, Updated: 0, Created: 72, Unchanged: 0, Deleted: 0, Errors: 0
				// Check the results
				System.out.println(op);
				assertEquals("Should be 0 test file that was deleted.", 0, op.deleted().size());
				assertEquals("Should be 72 test file that was created.", 72, op.created().size());
				assertEquals("Should be 0 test file that was updated.", 0, op.updated().size());
				assertEquals("Should be 72 total files.", 72, op.all().size());
				assertEquals("Should be 0 errors.", 0, op.errors().size());

				// Alter one of the local files
				File changedTestFile = new File(newTestDir, "testfile1");
				randomFiles.genFile(600, changedTestFile);

				// Create a new file
				File createdTestFile = new File(newTestDir, "testfile999");
				randomFiles.genFile(999, createdTestFile);
				allPaths.add(createdTestFile.getPath());

				// Delete a file
				File deletedFile = new File(newTestDir, "testfile2");
				deletedFile.delete();
				// Wait
				Thread.sleep(10000);
				// Do the sync
				op = runAndWatchProgress(
						() -> sftp.upload(newTestDir, resolveRemote(newTestDir.getName()), true, true, true),
						createProgress());
				// Check the results
				System.out.println(op);
				assertEquals("Should be 1 test file that was deleted.", 1, op.deleted().size());
				assertEquals("File deleted should be correct",
						resolveRemote(newTestDir.getName()) + "/" + deletedFile.getName(), op.deleted().get(0));
				assertEquals("Should be 1 test file that was created.", 1, op.created().size());
				assertEquals("File created should be correct",
						resolveRemote(newTestDir.getName()) + "/" + createdTestFile.getName(), op.created().get(0));
				assertEquals("Should be 1 test file that was updated.", 1, op.updated().size());
				assertEquals("File updated should correct",
						resolveRemote(newTestDir.getName()) + "/" + changedTestFile.getName(), op.updated().get(0));

				List<String> all = new ArrayList<>(op.all());
				Collections.sort(allPaths);
				Collections.sort(all);
				for (int i = 0; i < Math.max(allPaths.size(), all.size()); i++) {
					String l = i < allPaths.size() ? allPaths.get(i) : "";
					String r = i < all.size() ? all.get(i) : "";
					System.out.println(String.format("%2d : %s %-80s > %s", i, l.equals(r) ? "*" : " ", l, r));
				}

				assertEquals("All files should be synched.", allPaths.size(), op.all().size());
				// Compare
				compareDirs(newTestDir, newTestDir.getName());
			} finally {
				FileUtils.deleteDirectory(newTestDir);
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(5));
	}

	@Test
	public void testCopyToLocalSynchronize() throws Exception {
		timeout(() -> {
			// Copy our test files to the remote server first
			File testFilesDir = randomMultilevelFiles.getTestFilesDir();
			String resolveRemote = resolveRemote(randomMultilevelFiles.getName());
			runAndWatchProgress(
					() -> sftp.upload(testFilesDir, resolveRemote, true, false, true),
					createProgress());
			// Do stuff to the remote files
			sftp.rm(resolveRemote(randomMultilevelFiles.getName()) + "/testfile1");
			String newUpdatedFileContent = "This is an updated file";
			sftp.put(resolveRemote(randomMultilevelFiles.getName() + "/testfile2"),
					new ByteArrayInputStream(newUpdatedFileContent.getBytes()));
			sftp.put(resolveRemote(randomMultilevelFiles.getName() + "/testfile999"),
					new ByteArrayInputStream("This is a new file".getBytes()));
			File newTestDir = new File(testFilesDir.getParent(), testFilesDir.getName() + ".back");
			// Take a copy of the local files to synchronize against
			File localCopy = new File(newTestDir, randomMultilevelFiles.getName());
			FileUtils.copyDirectory(testFilesDir, localCopy);
			try {
				runAndWatchProgress(() -> 
					sftp.download(resolveRemote(randomMultilevelFiles.getName()), newTestDir,
						true, true, true), createProgress());
				// Compare local and remote
				compareDirs(newTestDir, testFilesDir.getName());
				// Check the changes on remote are local too
				assertFalse("testfile1 should not exist", new File(newTestDir, "testfile1").exists());
				assertTrue("testfile999 should have been created", new File(newTestDir, "testfile999").exists());
				assertEquals("testfile2 should have different content", newUpdatedFileContent,
						FileUtils.readFileToString(new File(newTestDir, "testfile2"), "UTF-8"));
			} finally {
				FileUtils.deleteDirectory(newTestDir);
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(3));
	}

	@Test
	public void testCopyToLocalRecursively() throws Exception {
		timeout(() -> {
			// Copy our test files first
			File testFilesDir = randomMultilevelFiles.getTestFilesDir();
			runAndWatchProgress(
					() -> sftp.upload(testFilesDir, resolveRemote(randomMultilevelFiles.getName()), true, false, true),
					createProgress());
			File newTestDir = new File(testFilesDir.getParent(), testFilesDir.getName() + ".back");
			try {
				runAndWatchProgress(() -> sftp.download(resolveRemote(randomMultilevelFiles.getName()), newTestDir,
						true, false, true), createProgress());
				// Compare local and remote
				compareDirs(newTestDir, testFilesDir.getName());
			} finally {
				FileUtils.deleteDirectory(newTestDir);
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(5));
	}

	//
	// Get
	//
	@Test
	public void testGet() throws Exception {
		timeout(() -> {
			// Put something to test
			for (File f : randomFiles.getTestFiles()) {
				SshConfiguration.getLogger().info("Uploading {0}", f);
				sftp.put(f, resolveRemote(f.getName()), TEST_FILE_PERMISSIONS);

				// Retrieve the file and test it
				File destination = new File(randomFiles.getLocalFilesDir(), f.getName());
				SshConfiguration.getLogger().info("Downloading {0}", f.getName());
				sftp.get(resolveRemote(f.getName()), destination);
				
				// Compare
				SshConfiguration.getLogger().info("Comparing {0}", f.getName());
				compare(f, destination);
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(2));
	}

	@Test
	public void testGetResume() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support resume.",
					ssh.getProvider().getCapabilities().contains(Capability.SFTP_RESUME));
			
			// We can reuse the same file. Truncate it to simulate a broken a
			// file
			testGet();
			// Put something to test
			for (File f : randomFiles.getTestFiles()) {
				File localFile = new File(randomFiles.getLocalFilesDir(), f.getName());
				File newFile = new File(randomFiles.getLocalFilesDir(), f.getName() + ".new");
				FileInputStream fileIn = new FileInputStream(localFile);
				long expectedLength = localFile.length();
				long halfLength = expectedLength / 2;
				try {
					FileOutputStream fileOut = new FileOutputStream(newFile);
					try {
						// Copy half the file
						IOUtils.copyLarge(fileIn, fileOut, 0, halfLength);
					} finally {
						fileOut.close();
					}
				} finally {
					fileIn.close();
				}
				localFile.delete();
				newFile.renameTo(localFile);
				// Now retrieve the file resuming
				CountingFileTransferProgress c = new CountingFileTransferProgress();
				runAndWatchProgress(() -> {
					try {
						sftp.resumeGet(resolveRemote(f.getName()), localFile);
					} catch (IOException ioe) {
					}
				}, c);
				assertEquals("File must have correct length", expectedLength, f.length());
				assertEquals("Must have only transferred half of file", halfLength, c.getTransferred());
				compare(f, localFile);
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(1));
	}

	@Test
	public void testVisit() throws Exception {
		timeout(() -> {
			// Copy our test files first
			runAndWatchProgress(
					() -> sftp.upload(randomFiles.getTestFilesDir(), "sftp-integration-tests", true, false, true),
					createProgress());
			sftp.addFileTransferListener(createProgress());
			AtomicInteger counter = new AtomicInteger();
			sftp.visit("sftp-integration-tests", new SftpFileVisitor() {
				@Override
				public FileVisitResult visitFile(SftpFile file, BasicFileAttributes attrs) throws IOException {
					if (file.getName().startsWith("test"))
						counter.incrementAndGet();
					return FileVisitResult.CONTINUE;
				}
			});
			assertEquals(randomFiles.getSize() / 2, counter.get());
			return null;
		}, TimeUnit.MINUTES.toMillis(3));
	}

	@Test
	public void testForcedKeyExchange() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Provider must support forcing key exchange.",
					ssh.getProvider().getCapabilities().contains(Capability.FORCE_KEY_EXCHANGE));
			// Put something to test
			long length = (long) (size().mib(256).toBytes());
			final Wrap<Boolean> w = new Wrap<Boolean>(true);
			Thread t = new Thread() {
				public void run() {
					try {
						while (true) {
							Thread.sleep(10000);
							if (w.get()) {
								System.out.println("Forcing key exchange");
								ssh.forceKeyExchange();
							} else {
								break;
							}
						}
					} catch (InterruptedException ie) {
					} catch (Exception ie) {
						ie.printStackTrace();
					}
				}
			};
			t.start();
			// Send the file
			String remote = "forceKeyExchangeTest";
			System.out.println("Sending");
			sftp.put(remote, new RandomizedInputStream(length), TEST_FILE_PERMISSIONS);
			// Retrieve the file and test it
			System.out.println("Retrieving");
			File file = new File(randomFiles.getLocalFilesDir(), remote);
			sftp.get(remote, file);
			// Stop forcing key exchange
			System.out.println("Retrieved");
			w.set(false);
			t.interrupt();
			t.join();
			// Check result file
			assertTrue("File must now exist locally", file.exists());
			assertEquals("File must have correct length", length, file.length());
			return null;
		}, TimeUnit.MINUTES.toMillis(2));
	}

	//
	// List files
	//
	@Test
	public void testLs() throws Exception {
		timeout(() -> {
			// Put something to test. 10 directories and 5 files
			for (int i = 0; i < 10; i++) {
				sftp.mkdir(resolveRemote("subdir" + i));
			}
			for (int i = 0; i < 5; i++) {
				sftp.put(resolveRemote("subfile") + i, new ByteArrayInputStream(new byte[10]), TEST_FILE_PERMISSIONS);
			}
			SftpFile[] files = sftp.ls(resolveRemote("/"));
			assertEquals("Must have correct number of files and directories (plus . and ..)",
					15 + (config.getServerCapabilities().contains(ServerCapability.SFTP_LS_RETURNS_DOTS)  && !ssh.getProvider().getCapabilities().contains(Capability.FILTERS_SFTP_DOT_DIRECTORIES) ? 2 : 0),
					files.length);
			return null;
		}, TimeUnit.SECONDS.toMillis(30));
	}

	@Test
	public void testList() throws Exception {
		timeout(() -> {
			// Put something to test. 10 directories and 5 files
			for (int i = 0; i < 10; i++)
				sftp.mkdir(resolveRemote("subdir" + i));
			for (int i = 0; i < 5; i++)
				sftp.put(resolveRemote("subfile" + i), new ByteArrayInputStream(new byte[10]), TEST_FILE_PERMISSIONS);
			int files = 0;
			for (@SuppressWarnings("unused")
			SftpFile f : sftp.list(resolveRemote("/")))
				files++;
			assertEquals("Must have correct number of files and directories (plus . and ..)",
					15 + (config.getServerCapabilities().contains(ServerCapability.SFTP_LS_RETURNS_DOTS) && !ssh.getProvider().getCapabilities().contains(Capability.FILTERS_SFTP_DOT_DIRECTORIES) ? 2 : 0),
					files);
			return null;
		}, TimeUnit.MINUTES.toMillis(1));
	}

	//
	// Put
	//
	// General put tests are covered in testGet()
	//
	@Test
	public void testPutResume() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support resume.",
					ssh.getProvider().getCapabilities().contains(Capability.SFTP_RESUME));
			
			// Put something to test
			for (File f : randomFiles.getTestFiles()) {
				File newFile = new File(randomFiles.getLocalFilesDir(), f.getName() + ".new");
				FileInputStream fileIn = new FileInputStream(f);
				long expectedLength = f.length();
				long halfLength = expectedLength / 2;
				try {
					FileOutputStream fileOut = new FileOutputStream(newFile);
					try {
						// Copy half the file
						IOUtils.copyLarge(fileIn, fileOut, 0, halfLength);
					} finally {
						fileOut.close();
					}
				} finally {
					fileIn.close();
				}
				// First put half the file
				String remotePath = resolveRemote(f.getName());
				SshConfiguration.getLogger().info("Uploading {0} (half length of {1} bytes)", newFile, newFile.length());
				sftp.put(newFile, remotePath);
				assertEquals("File must have correct remote length", halfLength, sftp.stat(remotePath).getSize());
				// Now resume
				CountingFileTransferProgress progress = new CountingFileTransferProgress();
				sftp.addFileTransferListener(progress);
				try {
					SshConfiguration.getLogger().info("Resuming {0} to {1}", f, remotePath);
					sftp.resumePut(f, remotePath);
				} catch (SftpException ex) {
					if (expectedLength > 0) {
						throw ex;
					} else {
						assertTrue("General error code or vendor specific code should be returned.",
								SftpException.GENERAL == ex.getCode() || ex.getCode().toString().startsWith("vendor-"));
					}
				} finally {
					sftp.removeFileTransferListener(progress);
				}
				assertEquals("File must have correct remote length", expectedLength, sftp.stat(remotePath).getSize());
				assertEquals("Must only have transferred the remainder of the file", halfLength,
						progress.getTransferred());
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(30));
	}

	//
	// Rename
	//
	@Test
	public void testRename() throws Exception {
		timeout(() -> {
			// Put something to test
			for (File f : randomFiles.getTestFiles()) {
				SshConfiguration.getLogger().info("Uploading {0} ({1} bytes)", f, f.length());
				sftp.put(f, resolveRemote(f.getName()), TEST_FILE_PERMISSIONS);
				String renamed = resolveRemote(f.getName() + ".renamed");
				try {
					sftp.rm(renamed);
				}
				catch(SshException sshe) {
				}
				SshConfiguration.getLogger().info("Renaming {0} to {1}", resolveRemote(f.getName()), renamed);
				sftp.rename(resolveRemote(f.getName()), renamed);
				try {
					sftp.stat(resolveRemote(f.getName()));
					assertTrue(f.getName() + " should no longer exist", false);
				} catch (SftpException s) {
				}
				assertEquals("Renamed file should exist and have correct length", f.length(),
						sftp.stat(renamed).getSize());
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	//
	// Remove
	//
	@Test
	public void testRemove() throws Exception {
		timeout(() -> {
			// Put something to test
			for (File f : randomFiles.getTestFiles()) {
				sftp.put(f, resolveRemote(f.getName()), TEST_FILE_PERMISSIONS);
				sftp.rm(resolveRemote(f.getName()));
				try {
					sftp.stat(resolveRemote(f.getName()));
					assertFalse(f.getName() + " should no longer exist", true);
				} catch (SftpException s) {
					assertEquals("Error should be file not found", SftpException.SSH_FX_NO_SUCH_FILE, s.getCode());
				}
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(60));
	}

	@Test(expected = SftpException.class)
	public void testRemoveDirectoryFail() throws Exception {
		timeout(() -> {
			sftp.mkdir(resolveRemote("testRemoveDirFail"), TEST_DIRECTORY_PERMISSIONS);
			sftp.put(resolveRemote("testRemoveDirFail/test1"), new RandomizedInputStream(Size.size().mb(5).toBytes()),
					TEST_FILE_PERMISSIONS);
			try {
				sftp.rm(resolveRemote("testRemoveDirFail"));
			} catch (SftpException s) {
				assertSftpStatus(s, SftpException.SSH_FX_FILE_IS_A_DIRECTORY);
			}
			return null;
		}, TimeUnit.SECONDS.toMillis(20));
	}

	@Test
	public void testRemoveDirectoryRecursive() throws Exception {
		timeout(() -> {
			sftp.mkdir(resolveRemote("testDir"));
			runAndWatchProgress(
					() -> sftp.upload(randomFiles.getTestFilesDir(), resolveRemote("testDir"), true, false, true),
					createProgress());
			sftp.rm(resolveRemote("testDir"), true);
			try {
				sftp.stat(resolveRemote("testDir"));
				assertFalse("testDir should no longer exist", true);
			} catch (SftpException s) {
				assertEquals("Error should be file not found", SftpException.SSH_FX_NO_SUCH_FILE, s.getCode());
			}
			return null;
		}, TimeUnit.MINUTES.toMillis(3));
	}

	//
	// Links
	//
	@Test
	public void testSymbolicLink() throws Exception {
		timeout(() -> {
			checkCapabilities(ServerCapability.SYMLINKS);
			File randomTestFile = randomFiles.getRandomTestFile();
			String targetPath = resolveRemote(randomTestFile.getName());
			sftp.put(randomTestFile, targetPath);
			String linkPath = resolveRemote(randomTestFile.getName() + ".link");
			sftp.symlink(linkPath, randomTestFile.getName());
			if(ssh.getProvider().getCapabilities().contains(Capability.SFTP_LSTAT))
				sftp.lstat(linkPath);
			else
				SshConfiguration.getLogger().warn("While the creation of a symlink of {0} did not throw any errors, we cannot tell if the link worked, as LSTAT is not implemented.", linkPath);
			return null;
		}, TimeUnit.SECONDS.toMillis(30000));
	}
	
	@Test
	public void testLink() throws Exception {
		timeout(() -> {
			checkCapabilities(ServerCapability.HARDLINKS);
			Assume.assumeTrue(ssh.getProvider().getCapabilities().contains(Capability.SFTP_HARD_LINK));
			File randomTestFile = randomFiles.getRandomTestFile();
			String targetPath = resolveRemote(randomTestFile.getName());
			sftp.put(randomTestFile, targetPath);
			String linkPath = resolveRemote(randomTestFile.getName() + ".link");
			sftp.link(linkPath, randomTestFile.getName());
			if(ssh.getProvider().getCapabilities().contains(Capability.SFTP_LSTAT))
				sftp.lstat(linkPath);
			else
				SshConfiguration.getLogger().warn("While the creation of a symlink of {0} did not throw any errors, we cannot tell if the link worked, as LSTAT is not implemented.", linkPath);
			return null;
		}, TimeUnit.SECONDS.toMillis(30000));
	}

	@Test
	public void testReadLink() throws Exception {
		timeout(() -> {
			Assume.assumeTrue(ssh.getProvider().getCapabilities().contains(Capability.SFTP_READ_LINK));
			checkCapabilities(ServerCapability.SYMLINKS);
			File randomTestFile = randomFiles.getRandomTestFile();
			String targetName = randomTestFile.getName();
			String targetPath = resolveRemote(targetName);
			sftp.put(randomTestFile, targetPath);
			String linkPath = resolveRemote(targetName + ".link");
			sftp.symlink(linkPath, targetName);
			String originalPath = sftp.readLink(linkPath);
			assertEquals("Link target must point to original file", targetPath,
					originalPath);
			return null;
		}, TimeUnit.SECONDS.toMillis(30000));
	}

	@Test
	public void testV3TextMode() throws Exception {
		timeout(() -> {
			Assume.assumeTrue(sftp.getSftpVersion() <= 3);
			Assume.assumeTrue(ssh.getProvider().getCapabilities().contains(Capability.SFTP_TRANSFER_MODE));
			String CRLFText = "line1\r\nline2\r\nline3\r\n";
			String LFText = "line1\nline2\nline3\n";
			String CRText = "line1\rline2\rline3\r";
			doPutTextFile(1, LFText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_CR_LF);
			doPutTextFile(2, LFText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_CR);
			doPutTextFile(3, LFText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_LF);
			doPutTextFile(4, CRLFText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_CR_LF);
			doPutTextFile(5, CRLFText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_CR);
			doPutTextFile(6, CRLFText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_LF);
			doPutTextFile(7, CRText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_CR_LF);
			doPutTextFile(8, CRText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_CR);
			doPutTextFile(9, CRText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_LF);
			doGetTextFile(10, CRLFText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_CR_LF);
			doGetTextFile(11, CRText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_CR);
			doGetTextFile(12, LFText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, EOLPolicy.REMOTE_LF);
			doGetTextFile(13, CRLFText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_CR_LF);
			doGetTextFile(14, CRText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_CR);
			doGetTextFile(15, LFText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, EOLPolicy.REMOTE_LF);
			doGetTextFile(16, CRLFText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_CR_LF);
			doGetTextFile(17, CRText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_CR);
			doGetTextFile(18, LFText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, EOLPolicy.REMOTE_LF);
			return null;
		}, TimeUnit.SECONDS.toMillis(30));
	}

	@Test
	public void testV4TextMode() throws Exception {
		timeout(() -> {
			Assume.assumeTrue(sftp.getSftpVersion() > 3);
			Assume.assumeTrue(ssh.getProvider().getCapabilities().contains(Capability.SFTP_TRANSFER_MODE));
			String CRLFText = "line1\r\nline2\r\nline3\r\n";
			String LFText = "line1\nline2\nline3\n";
			String CRText = "line1\rline2\rline3\r";
			String remoteText = "line1" + sftp.getRemoteEOL() + "line2" + sftp.getRemoteEOL() + "line3"
					+ sftp.getRemoteEOL();
			doPutTextFile(1, LFText, remoteText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, sftp.getRemoteEOL());
			doPutTextFile(2, CRLFText, remoteText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, sftp.getRemoteEOL());
			doPutTextFile(3, CRText, remoteText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, sftp.getRemoteEOL());
			doGetTextFile(4, remoteText, LFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_LF, sftp.getRemoteEOL());
			doGetTextFile(5, remoteText, CRLFText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR_LF, sftp.getRemoteEOL());
			doGetTextFile(6, remoteText, CRText, EOLPolicy.FORCE_REMOTE, EOLPolicy.LOCAL_CR, sftp.getRemoteEOL());
			return null;
		}, TimeUnit.MINUTES.toMillis(2));
	}

	private void runAndWatchProgress(Runnable runnable, SshFileTransferListener listener) throws Exception {
		try {
			sftp.addFileTransferListener(listener);
			runnable.run();
		} finally {
			sftp.removeFileTransferListener(listener);
		}
	}

	private <T> T runAndWatchProgress(Callable<T> runnable, SshFileTransferListener listener) throws Exception {
		try {
			sftp.addFileTransferListener(listener);
			return runnable.call();
		} finally {
			sftp.removeFileTransferListener(listener);
		}
	}

	private void doPutTextFile(int idx, String text, String expectedText, EOLPolicy... eol)
			throws UnsupportedEncodingException, SftpException, SshException {
		sftp.mode(TransferMode.TEXT);
		sftp.eol(eol);
		sftp.put("file.txt", new ByteArrayInputStream(text.getBytes("UTF-8")), TEST_FILE_PERMISSIONS);
		sftp.mode(TransferMode.BINARY);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		sftp.get("file.txt", out);
		String actualText = new String(out.toByteArray(), "UTF-8");
		assertEquals("PUT Text Comparision [" + idx + "] for " + Arrays.asList(eol), expectedText, actualText);
	}

	private void doGetTextFile(int idx, String text, String expectedText, EOLPolicy... eol)
			throws UnsupportedEncodingException, SftpException, SshException {
		sftp.mode(TransferMode.BINARY);
		sftp.eol(eol);
		// sftp.setForceRemoteEOL(true);
		// sftp.setRemoteEOL(remoteEOL);
		// sftp.setLocalEOL(localEOL);
		sftp.put("file.txt", new ByteArrayInputStream(text.getBytes("UTF-8")), TEST_FILE_PERMISSIONS);
		sftp.mode(TransferMode.TEXT);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		sftp.get("file.txt", out);
		String actualText = new String(out.toByteArray(), "UTF-8");
		assertEquals("GET Text Comparision [" + idx + "] for " + Arrays.asList(eol), expectedText, actualText);
	}
}
