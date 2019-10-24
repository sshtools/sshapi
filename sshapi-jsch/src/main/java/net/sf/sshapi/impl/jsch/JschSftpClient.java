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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferListener;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpOutputStream;

class JschSftpClient extends AbstractSftpClient {
	private final ChannelSftp channel;
	private String home;

	public JschSftpClient(SshProvider provider, ChannelSftp channel, SshConfiguration configuration) {
		super(provider, configuration);
		this.channel = channel;
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

	@Override
	public void chmod(final String path, final int permissions) throws SshException {
		try {
			setPermissions(path, permissions);
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
	public OutputStream doPut(final String path, long offset) throws SshException {
		try {
			OutputStream out = channel.put(path, null, ChannelSftp.OVERWRITE, offset);
			return new SftpOutputStream(out, this, path, out.toString());
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public String getDefaultPath() throws SshException {
		return home;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SftpFile[] ls(String path) throws SshException {
		Vector<ChannelSftp.LsEntry> paths;
		try {
			paths = channel.ls(path);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
		List<SftpFile> files = new ArrayList<>();
		for (ChannelSftp.LsEntry entry : paths) {
			SftpFile file = new SftpFile(convertType(entry.getAttrs()), path + "/" + entry.getFilename(),
					entry.getAttrs().getSize(), convertIntDate(entry.getAttrs().getMTime()), 0,
					convertIntDate(entry.getAttrs().getATime()), entry.getAttrs().getGId(), entry.getAttrs().getUId(),
					entry.getAttrs().getPermissions());
			files.add(file);
		}
		return files.toArray(new SftpFile[0]);
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			channel.mkdir(path);
			if (permissions > -1)
				setPermissions(path, permissions);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void onClose() throws SshException {
		channel.disconnect();
	}

	@Override
	public void onOpen() throws SshException {
		try {
			try {
				if (configuration.getSftpPacketSize() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalPacketSize", int.class);
					m.setAccessible(true);
					m.invoke(channel, (int) configuration.getSftpPacketSize());
				}
				if (configuration.getSftpWindowSize() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalWindowSize", int.class);
					m.setAccessible(true);
					m.invoke(channel, (int) configuration.getSftpWindowSize());
				}
				if (configuration.getSftpWindowSizeMax() > 0) {
					Method m = Channel.class.getDeclaredMethod("setLocalWindowSizeMax", int.class);
					m.setAccessible(true);
					m.invoke(channel, (int) configuration.getSftpWindowSizeMax());
				}
			} catch (Exception e) {
				SshConfiguration.getLogger().log(Level.DEBUG, "Failed to set SFTP channel configuration via reflection.", e);
			}
			channel.connect();
			home = channel.pwd();
		} catch (JSchException e) {
			throw new SshException("Failed to open SFTP client.", e);
		} catch (SftpException e) {
			throw new SshException("Failed to get home directory.", e);
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
	public SftpFile stat(String path) throws SshException {
		try {
			SftpATTRS attrs = channel.stat(path);
			return new SftpFile(convertType(attrs), path, attrs.getSize(), attrs.getMTime() * 1000, 0, attrs.getATime() * 1000,
					attrs.getGId(), attrs.getUId(), attrs.getPermissions());
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			channel.symlink(path, target);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected InputStream doGet(String path, long filePointer) throws SshException {
		try {
			if (filePointer > 0)
				throw new UnsupportedOperationException("Cannot download from an arbitrary position using this provider.");
			return channel.get(path, createMonitor(path, SshFileTransferListener.DEFAULT_SOURCE_OR_TARGET, 0), filePointer);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void doGet(String path, OutputStream out, long filePointer) throws SshException {
		try {
			if (filePointer > 0)
				throw new UnsupportedOperationException("Cannot download from an arbitrary position using this provider.");
			channel.get(path, out, createMonitor(path, out.toString(), 0), ChannelSftp.OVERWRITE, filePointer);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void resumeGet(String path, File destination) throws SshException {
		try {
			/* Jsch fires progress events that cover the complete file so we offset by the existing length */
			channel.get(path, destination.getAbsolutePath(), createMonitor(path, destination.toString(), destination.length()), ChannelSftp.RESUME);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void resumePut(File source, String path) throws SshException {
		try {
			/* Jsch fires progress events that cover the complete file so we offset by the existing length.
			 * For uploads, this means we need to know the remote size, so use a stat() */
			long offset = 0;
			try {
				SftpFile f = stat(path);
				offset = f.getSize();
			} catch(net.sf.sshapi.sftp.SftpException se) {
			}
			channel.put(source.getAbsolutePath(), path, createMonitor(source.toString(), path, offset), ChannelSftp.RESUME);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		try {
			channel.put(in, path, createMonitor(path, in.toString(), 0), ChannelSftp.OVERWRITE);
		} catch (SftpException e) {
			throw new net.sf.sshapi.sftp.SftpException(e.id, e.getLocalizedMessage(), e);
		}
	}

	long convertIntDate(int date) {
		return Integer.toUnsignedLong(date) * 1000l;
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

	private SftpProgressMonitor createMonitor(String path, String target, long offset) {
		return new SftpProgressMonitor() {
			private long total;
			
			@Override
			public boolean count(long count) {
				total += count;
				fireFileTransferProgressed(path, target, total);
				return true;
			}

			@Override
			public void end() {
				fireFileTransferFinished(path, target);
			}

			@Override
			public void init(int op, String src, String dest, long max) {
				total -= offset;
				fireFileTransferStarted(path, target, max - offset);
			}
		};
	}

	private void setPermissions(String path, int permissions) throws SftpException {
		if (permissions > -1) {
			SftpATTRS attrs = channel.stat(path);
			attrs.setPERMISSIONS(permissions);
			channel.setStat(path, attrs);
		}
	}
}
