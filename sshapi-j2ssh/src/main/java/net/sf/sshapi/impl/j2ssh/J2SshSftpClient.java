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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.sftp.FileAttributes;

class J2SshSftpClient extends AbstractSftpClient {

	private final SshClient client;
	private SftpClient sftpClient;
	private String home;

	public J2SshSftpClient(SshClient client) {
		this.client = client;
	}

	public void onClose() throws SshException {
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

	private SftpFile entryToFile(String path, com.sshtools.j2ssh.sftp.SftpFile entry) throws com.sshtools.j2ssh.SshException {
		FileAttributes attr = entry.getAttributes();
		SftpFile file = new SftpFile(convertType(attr), entry.getAbsolutePath(), attr.getSize().longValue(), toLong(attr.getModifiedTime()), 0,
			toLong(attr.getAccessedTime()), (int) toLong(attr.getGID()), (int) toLong(attr.getUID()), (int) toLong(attr
				.getPermissions()));
		return file;
	}

	long convertIntDate(Integer date) {
		return date == null ? 0 : date.intValue() * 1000l;
	}

	public void onOpen() throws SshException {
		try {
			sftpClient = client.openSftpClient();
			home = sftpClient.pwd();
		} catch (IOException e) {
			throw new SshException("Failed to open SFTP client.", e);
		}
	}

	public String getDefaultPath() throws SshException {
		return home;
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

	public SftpFile stat(String path) throws SshException {
		try {
			FileAttributes entry = sftpClient.stat(path);
			return entryToFile(path, entry);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	private SftpFile entryToFile(String path, FileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.getSize().longValue(), toLong(entry.getModifiedTime()), 0, toLong(entry
			.getAccessedTime()), (int) toLong(entry.getGID()), (int) toLong(entry.getUID()), (int) toLong(entry.getPermissions()));
	}

	long toLong(UnsignedInteger32 i) {
		return i == null ? 0 : i.intValue();
	}

	int toInt(Integer val) {
		return val == null ? 0 : val.intValue();
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

	public void get(String path, OutputStream out) throws SshException {
		try {
			sftpClient.get(path, out);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void put(String path, InputStream in, int permissions) throws SshException {
		try {
			sftpClient.put(in, path);
			sftpClient.chmod(permissions, path);
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

	public void rename(String path, String newPath) throws SshException {
		try {
			sftpClient.rename(path, newPath);
		} catch (IOException e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void chgrp(String path, int permissions) throws SshException {
		try {
			sftpClient.chgrp(permissions, path);
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

	public void setLastModified(String path, long modtime) throws SshException {
		throw new UnsupportedOperationException();
	}
}
