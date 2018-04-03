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
package net.sf.sshapi.impl.maverick;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpFileAttributes;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.sftp.SftpSubsystemChannel;
import com.sshtools.ssh.SshClient;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.Util;

class MaverickSftpClient extends AbstractSftpClient {

	private final SshClient client;
	private SftpClient sftpClient;
	private net.sf.sshapi.SshClient sshapiClient;
	private String home;

	MaverickSftpClient(net.sf.sshapi.SshClient sshapiClient, SshClient client) {
		this.client = client;
		this.sshapiClient = sshapiClient;
	}

	public void onClose() throws SshException {
		try {
			sftpClient.exit();
		} catch (com.sshtools.ssh.SshException sshe) {
			throw new SshException(SshException.GENERAL, sshe);
		}
	}

	public SftpFile[] ls(String path) throws SshException {
		try {
			com.sshtools.sftp.SftpFile[] entries = sftpClient.ls(path);
			SftpFile[] files = new SftpFile[entries.length];
			for (int i = 0; i < entries.length; i++) {
				files[i] = entryToFile(path, entries[i]);
			}
			return files;
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, com.sshtools.sftp.SftpFile entry) throws SftpStatusException,
			com.sshtools.ssh.SshException {
		String fullPath = Util.concatenatePaths(path, entry.getFilename());
		SftpFileAttributes attr = entry.getAttributes();
		SftpFile file = new SftpFile(convertType(attr), fullPath, attr.getSize().longValue(), attr.getModifiedDateTime().getTime(),
			attr.getCreationTime().longValue(), attr.getAccessedTime().longValue(), toInt(attr.getGID()), toInt(attr.getUID()),
			attr.getPermissions().intValue());
		return file;
	}

	long convertIntDate(Integer date) {
		return date == null ? 0 : date.intValue() * 1000l;
	}

	public void onOpen() throws SshException {
		try {
			// int sftpMode = SftpClientFactory.class.getField(
			// sshapiClient.getConfiguration().getProperties().getProperty(MaverickSshProvider.CFG_SFTP_MODE,
			// "SFTP_ALL_MODES"))
			// .getInt(null);
			int sftpMaxVersion = Integer.parseInt(sshapiClient.getConfiguration().getProperties()
				.getProperty(MaverickSshProvider.CFG_SFTP_MAX_VERSION, String.valueOf(SftpSubsystemChannel.MAX_VERSION)));
			sftpClient = new SftpClient(client, sftpMaxVersion);
			home = sftpClient.pwd();
			// sftpClient =
			// SftpClientFactory.getInstance().createSftpClient(client,
			// sftpMaxVersion, sftpMode);
		} catch (Exception e) {
			throw new SshException("Failed to open SFTP client.", e);
		}
	}

	public String getDefaultPath() throws SshException {
		return home;
	}

	public void mkdir(String path, int permissions) throws SshException {
		try {
			sftpClient.mkdir(path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void mkdirs(String path, int permissions) throws SshException {
		try {
			sftpClient.mkdirs(path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directorys.", e);
		}
	}

	public void rm(String path) throws SshException {
		try {
			SftpFile file = stat(path);
			if (file.isDirectory()) {
				throw new SftpException(SftpException.SSH_FX_FILE_IS_A_DIRECTORY);
			}
			sftpClient.rm(path);
		} catch (SftpException e) {
			throw e;
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
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
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (SftpException e) {
			throw e;
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public SftpFile stat(String path) throws SshException {
		try {
			SftpFileAttributes entry = sftpClient.stat(path);
			return entryToFile(path, entry);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SftpFileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.getSize().longValue(), entry.getModifiedDateTime().getTime(), entry
			.getCreationTime().longValue(), entry.getAccessedTime().longValue(), toInt(entry.getGID()), toInt(entry.getUID()),
			entry.getPermissions().intValue());
	}

	int toInt(String val) {
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
		}
		return 0;
	}

	int convertType(SftpFileAttributes attrs) {
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

	public InputStream get(String path, long filePointer) throws SshException {
		try {
			return sftpClient.getInputStream(path, filePointer);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for reading.", e);
		}
	}

	public InputStream get(String path) throws SshException {
		try {
			return sftpClient.getInputStream(path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for reading.", e);
		}
	}

	public OutputStream put(final String path, final int permissions) throws SshException {
		try {
			final OutputStream pout = sftpClient.getOutputStream(path);
			return new FilterOutputStream(pout) {
				public void write(byte b[], int off, int len) throws IOException {
					// Never did get why this is needed ???
					out.write(b, off, len);
				}

				public void close() throws IOException {
					super.close();
					if (permissions > -1) {
						try {
							sftpClient.chmod(permissions, path);
						} catch (SftpStatusException e) {
							IOException ioe = new IOException("Failed to set permissions on file close.");
							ioe.initCause(e);
							throw ioe;
						} catch (com.sshtools.ssh.SshException e) {
							IOException ioe = new IOException("Failed to set permissions on file close.");
							ioe.initCause(e);
							throw ioe;
						}
					}
				}
			};
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for writing.", e);
		}
	}

	public void chmod(final String path, final int permissions) throws SshException {
		try {
			sftpClient.chmod(permissions, path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	public void get(String path, OutputStream out, long filePointer) throws SshException {
		try {
			sftpClient.get(path, out, filePointer);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void get(String path, OutputStream out) throws SshException {
		try {
			sftpClient.get(path, out);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void put(String path, InputStream in, int permissions) throws SshException {
		try {
			sftpClient.put(in, path);
			if (permissions > -1) {
				sftpClient.chmod(permissions, path);
			}
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to write remote file.", e);
		}
	}

	public void rename(String path, String newPath) throws SshException {
		try {
			sftpClient.rename(path, newPath);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	public void setLastModified(String path, long modtime) throws SshException {
		// Not support in this version
		
//		try {
//			sftpClient.setLastModified(modtime, path);
//		} catch (SftpStatusException sftpE) {
//			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
//		} catch (Exception e) {
//			throw new SshException("Failed to create directory.", e);
//		}

		throw new UnsupportedOperationException();
	}

	public void chown(String path, int uid) throws SshException {
		try {
			sftpClient.chown(String.valueOf(uid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	public void chgrp(String path, int gid) throws SshException {
		try {
			sftpClient.chgrp(String.valueOf(gid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}
}
