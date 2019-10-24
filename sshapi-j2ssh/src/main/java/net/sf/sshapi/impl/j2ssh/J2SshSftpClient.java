/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.impl.j2ssh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sshtools.j2ssh.DirectoryOperation;
import com.sshtools.j2ssh.FileTransferProgress;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpOperation;

class J2SshSftpClient extends AbstractSftpClient {
	private final SshClient client;
	private String home;
	private SftpClient sftpClient;

	public J2SshSftpClient(J2SshClient sshapiClient, SshClient client) {
		super(sshapiClient.getProvider(), sshapiClient.getConfiguration());
		this.client = client;
	}

	public void chgrp(String path, int permissions) throws SshException {
		try {
			sftpClient.chgrp(permissions, path);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void chmod(String path, int permissions) throws SshException {
		try {
			sftpClient.chmod(permissions, path);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void chown(String path, int permissions) throws SshException {
		try {
			sftpClient.chown(permissions, path);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toOperation(sftpClient.copyRemoteDirectory(remotedir, localdir.getAbsolutePath(), recurse, sync, commit,
					createProgress(localdir.getPath())));
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public String getDefaultPath() throws SshException {
		return home;
	}

	public SftpFile[] ls(String path) throws SshException {
		try {
			@SuppressWarnings("unchecked")
			List<com.sshtools.j2ssh.sftp.SftpFile> entries = sftpClient.ls(path);
			SftpFile[] files = new SftpFile[entries.size()];
			for (int i = 0; i < files.length; i++) {
				files[i] = entryToFile(path, entries.get(i));
			}
			return files;
		} catch (IOException e) {
			throw new SshException("Failed to list directory.", e);
		}
	}

	public void mkdir(String path, int permissions) throws SshException {
		try {
			sftpClient.mkdir(path);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void mkdirs(String path, int permissions) throws SshException {
		sftpClient.mkdirs(path);
	}

	public void onClose() throws SshException {
	}

	public void onOpen() throws SshException {
		try {
			sftpClient = client.openSftpClient();
			home = sftpClient.pwd();
		} catch (IOException e) {
			throw new SshException("Failed to open SFTP client.", e);
		}
	}

	public void rename(String path, String newPath) throws SshException {
		try {
			sftpClient.rename(path, newPath);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void rm(String path) throws SshException {
		try {
			SftpFile file = stat(path);
			if (file.isDirectory()) {
				throw new SftpException(SftpException.SSH_FX_NO_SUCH_FILE);
			}
			sftpClient.rm(path);
		} catch (SftpException e) {
			throw e;
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void rm(String path, boolean recurse) throws SftpException, SshException {
		try {
			sftpClient.rm(path, true, recurse);
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void rmdir(String path) throws SshException {
		try {
			SftpFile file = stat(path);
			if (!file.isDirectory()) {
				throw new SftpException(SftpException.SSH_FX_NOT_A_DIRECTORY);
			}
			sftpClient.rm(path);
		} catch (SftpException e) {
			throw e;
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void setLastModified(String path, long modtime) throws SshException {
		throw new UnsupportedOperationException();
	}

	public SftpFile stat(String path) throws SshException {
		try {
			FileAttributes entry = sftpClient.stat(path);
			return entryToFile(path, entry);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			sftpClient.symlink(path, target);
		} catch (SftpException e) {
			throw e;
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public SftpOperation upload(File localdir, String remotedir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toOperation(sftpClient.copyLocalDirectory(localdir.getAbsolutePath(), remotedir, recurse, sync, commit,
					createProgress(localdir.getPath())));
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected void doDownload(String path, OutputStream out) throws SshException {
		try {
			sftpClient.get(path, out, createProgress(out.toString()));
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		try {
			sftpClient.put(in, path, createProgress(in.toString()));
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	long convertIntDate(Integer date) {
		return date == null ? 0 : date.intValue() * 1000l;
	}

	int convertType(FileAttributes attrs) {
		if (attrs.isDirectory()) {
			return SftpFile.TYPE_DIRECTORY;
		} else if (attrs.isLink()) {
			return SftpFile.TYPE_LINK;
		} else if (attrs.isFile()) {
			return SftpFile.TYPE_FILE;
		} else if (attrs.isFifo()) {
			return SftpFile.TYPE_FIFO;
		} else if (attrs.isCharacter()) {
			return SftpFile.TYPE_CHARACTER;
		} else if (attrs.isBlock()) {
			return SftpFile.TYPE_BLOCK;
		} else {
			return SftpFile.TYPE_UNKNOWN;
		}
	}

	int toInt(Integer val) {
		return val == null ? 0 : val.intValue();
	}

	long toLong(UnsignedInteger32 i) {
		return i == null ? 0 : i.intValue();
	}

	private FileTransferProgress createProgress(String target) {
		return new FileTransferProgress() {
			private String path;

			@Override
			public void completed() {
				fireFileTransferFinished(path, target);
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public void progressed(long progress) {
				fireFileTransferProgressed(path, target, progress);
			}

			@Override
			public void started(long length, String path) {
				this.path = path;
				fireFileTransferStarted(path, target, length);
			}
		};
	}

	private SftpFile entryToFile(String path, com.sshtools.j2ssh.sftp.SftpFile entry) throws com.sshtools.j2ssh.SshException {
		FileAttributes attr = entry.getAttributes();
		SftpFile file = new SftpFile(convertType(attr), entry.getAbsolutePath(), attr.getSize().longValue(),
				toLong(attr.getModifiedTime()), 0, toLong(attr.getAccessedTime()), (int) toLong(attr.getGID()),
				(int) toLong(attr.getUID()), (int) toLong(attr.getPermissions()));
		return file;
	}

	private SftpFile entryToFile(String path, FileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.getSize().longValue(), toLong(entry.getModifiedTime()), 0,
				toLong(entry.getAccessedTime()), (int) toLong(entry.getGID()), (int) toLong(entry.getUID()),
				(int) toLong(entry.getPermissions()));
	}

	private SftpOperation toOperation(DirectoryOperation op) {
		List<String> updated = new ArrayList<String>();
		for (Object f : op.getUpdatedFiles())
			updated.add(f.toString());
		List<String> unchanged = new ArrayList<String>();
		for (Object f : op.getUnchangedFiles())
			unchanged.add(f.toString());
		List<String> deleted = new ArrayList<String>();
		for (Object f : op.getDeletedFiles())
			deleted.add(f.toString());
		List<String> created = new ArrayList<String>();
		for (Object f : op.getNewFiles())
			created.add(f.toString());
		Set<String> all = new LinkedHashSet<>();
		all.addAll(updated);
		all.addAll(unchanged);
		all.addAll(deleted);
		all.addAll(created);
		Map<String, Exception> errors = new HashMap<String, Exception>();
		List<String> allList = new ArrayList<>(all);
		return new SftpOperation() {
			@Override
			public List<String> all() {
				return allList;
			}

			@Override
			public List<String> created() {
				return created;
			}

			@Override
			public List<String> deleted() {
				return deleted;
			}

			@Override
			public Map<String, Exception> errors() {
				return errors;
			}

			@Override
			public long files() {
				return op.getFileCount();
			}

			@Override
			public long size() {
				return op.getTransferSize();
			}

			@Override
			public List<String> unchanged() {
				return unchanged;
			}

			@Override
			public List<String> updated() {
				return updated;
			}
		};
	}
}
