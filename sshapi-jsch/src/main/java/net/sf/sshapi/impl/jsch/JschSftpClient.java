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
package net.sf.sshapi.impl.jsch;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

class JschSftpClient extends AbstractSftpClient {

	private final ChannelSftp channel;
	private String home;

	public JschSftpClient(ChannelSftp channel) {
		this.channel = channel;
	}

	@Override
	public void onClose() throws SshException {
		channel.disconnect();
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		Vector paths;
		try {
			paths = channel.ls(path);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
		List files = new ArrayList();
		for (Enumeration e = paths.elements(); e.hasMoreElements();) {
			ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) e.nextElement();
			SftpFile file = new SftpFile(convertType(entry.getAttrs()), entry.getFilename(), entry.getAttrs().getSize(),
				convertIntDate(entry.getAttrs().getMTime()), 0, convertIntDate(entry.getAttrs().getATime()), entry.getAttrs()
					.getGId(), entry.getAttrs().getUId(), entry.getAttrs().getPermissions());
			files.add(file);
		}
		return (SftpFile[]) files.toArray(new SftpFile[paths.size()]);
	}

	long convertIntDate(int date) {
		return date * 1000l;
	}

	@Override
	public void onOpen() throws SshException {
		try {
			channel.connect();
			home = channel.pwd();
		} catch (JSchException e) {
			throw new SshException("Failed to open SFTP client.", e);
		} catch (SftpException e) {
			throw new SshException("Failed to get home directory.", e);
		}
	}

	@Override
	public String getDefaultPath() throws SshException {
		return home;
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			channel.mkdir(path);
			setPermissions(path, permissions);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}

	}

	@Override
	public void rm(String path) throws SshException {
		try {
			channel.rm(path);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		try {
			channel.rmdir(path);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		try {
			SftpATTRS attrs = channel.stat(path);
			return new SftpFile(convertType(attrs), path, attrs.getSize(), attrs.getMTime() * 1000, 0, attrs.getATime() * 1000,
				attrs.getGId(), attrs.getUId(), attrs.getPermissions());
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	int convertType(SftpATTRS attrs) {
		if (attrs.isDir()) {
			return SftpFile.TYPE_DIRECTORY;
		} else if (attrs.isLink()) {
			return SftpFile.TYPE_LINK;
		} else {
			return SftpFile.TYPE_FILE;
		}
	}

	@Override
	public void get(String path, OutputStream out) throws SshException {
		try {
			channel.get(path, out);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void get(String path, OutputStream out, long filePointer) throws SshException {
		try {
			channel.get(path, out, null, ChannelSftp.OVERWRITE, filePointer);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public InputStream get(String path, long filePointer) throws SshException {
		try {
			return channel.get(path, null, filePointer);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public InputStream get(String path) throws SshException {
		try {
			return channel.get(path);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void put(String path, InputStream in, int permissions) throws SshException {
		try {
			channel.put(in, path, ChannelSftp.OVERWRITE);
			setPermissions(path, permissions);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public OutputStream put(final String path, final int permissions) throws SshException {
		return put(path, permissions, 0);
	}

	@Override
	public OutputStream put(final String path, final int permissions, long offset) throws SshException {
		try {
			final OutputStream out = channel.put(path, null, ChannelSftp.OVERWRITE, offset);
			return new FilterOutputStream(out) {
				@Override
				public void write(byte b[], int off, int len) throws IOException {
					// Never did get why this is needed ???
					out.write(b, off, len);
				}

				@Override
				public void close() throws IOException {
					super.close();
					try {
						setPermissions(path, permissions);
					} catch (Exception e) {
						IOException ioe = new IOException("Failed to set permissions on file close.");
						ioe.initCause(e);
						throw ioe;
					}
				}
			};
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void chmod(final String path, final int permissions) throws SshException {
		try {
			setPermissions(path, permissions);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	private void setPermissions(String path, int permissions) throws SftpException {
		if (permissions > -1) {
			SftpATTRS attrs = channel.stat(path);
			attrs.setPERMISSIONS(permissions);
			channel.setStat(path, attrs);
		}
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		try {
			channel.rename(path, newPath);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}

	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			SftpATTRS attrs = channel.stat(path);
			attrs.setACMODTIME(attrs.getMTime(), (int) modtime / 1000);
			channel.setStat(path, attrs);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}

	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			SftpATTRS attrs = channel.stat(path);
			attrs.setUIDGID(uid, attrs.getGId());
			channel.setStat(path, attrs);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}

	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			SftpATTRS attrs = channel.stat(path);
			attrs.setUIDGID(attrs.getUId(), gid);
			channel.setStat(path, attrs);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}

	}

}
