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
package net.sf.sshapi.impl.maverick;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sshtools.sftp.FileTransferProgress;
import com.sshtools.sftp.SftpClient;
import com.sshtools.sftp.SftpFileAttributes;
import com.sshtools.sftp.SftpStatusException;
import com.sshtools.sftp.SftpSubsystemChannel;
import com.sshtools.ssh.SshClient;
import com.sshtools.util.UnsignedInteger64;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.sftp.SftpInputStream;
import net.sf.sshapi.sftp.SftpOperation;
import net.sf.sshapi.sftp.SftpOutputStream;
import net.sf.sshapi.util.Util;

class MaverickSftpClient extends AbstractSftpClient<MaverickSshClient> {
	private final SshClient client;
	private SftpClient sftpClient;
	private String home;
	private int defaultRemoteEOL = SftpClient.EOL_CRLF;

	MaverickSftpClient(MaverickSshClient client) {
		super(client);
		this.client = client.getNativeClient();
	}

	@Override
	public void onClose() throws SshException {
		try {
			sftpClient.exit();
		} catch (com.sshtools.ssh.SshException sshe) {
			throw new SshException(SshException.GENERAL, sshe);
		}
	}

	@Override
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

	private SftpFile entryToFile(String path, com.sshtools.sftp.SftpFile entry)
			throws SftpStatusException, com.sshtools.ssh.SshException {
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

	@Override
	public void onOpen() throws SshException {
		try {
			// int sftpMode = SftpClientFactory.class.getField(
			// sshapiClient.getConfiguration().getProperties().getProperty(MaverickSshProvider.CFG_SFTP_MODE,
			// "SFTP_ALL_MODES"))
			// .getInt(null);
			int sftpMaxVersion = Integer.parseInt(configuration.getProperties()
					.getProperty(MaverickSshProvider.CFG_SFTP_MAX_VERSION, String.valueOf(SftpSubsystemChannel.MAX_VERSION)));
			sftpClient = new SftpClient(client, sftpMaxVersion);
			home = sftpClient.pwd();
			if (eolPolicy != null)
				onEOLPolicyChange(eolPolicy);
			if (transferMode != null)
				onTransferModeChange(transferMode);
			// sftpClient =
			// SftpClientFactory.getInstance().createSftpClient(client,
			// sftpMaxVersion, sftpMode);
		} catch (Exception e) {
			throw new SshException("Failed to open SFTP client.", e);
		}
	}

	@Override
	public String getDefaultPath() {
		return home;
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			sftpClient.mkdir(path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void mkdirs(String path, int permissions) throws SshException {
		try {
			sftpClient.mkdirs(path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directorys.", e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			String linkpath = Util.getAbsolutePath(path, getDefaultPath());
			String targetpath = Util.getAbsolutePath(target, Util.dirname(linkpath));
			switch(configuration.getSftpSymlinks()) {
			case SshConfiguration.STANDARD_SFTP_SYMLINKS:
				sftpClient.getSubsystemChannel().createSymbolicLink(targetpath, linkpath);
				break;
			case SshConfiguration.OPENSSH_SFTP_SYMLINKS:
				sftpClient.getSubsystemChannel().createSymbolicLink(linkpath, targetpath);
				break;
			default:
				if(isOpenSSH()) {
					sftpClient.getSubsystemChannel().createSymbolicLink(linkpath, targetpath);
				}
				else {
					sftpClient.getSubsystemChannel().createSymbolicLink(targetpath, linkpath);
				}
				break;
			}
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws net.sf.sshapi.sftp.SftpException {
		try {
			return createHandle(sftpClient.getSubsystemChannel().openFile(path, OpenMode.toFlags(modes)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException ioe) {
			throw new SftpException(SftpException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	private SftpHandle createHandle(com.sshtools.sftp.SftpFile nativeHandle) {
		return new SftpHandle() {
			private long position;
			private byte[] readBuffer;
			private byte[] writeBuffer;

			@Override
			public void close() throws IOException {
				try {
					try {
						nativeHandle.close();
					} catch (SftpStatusException e) {
						throw new IOException("Failed to close.", e);
					} catch (com.sshtools.ssh.SshException e) {
						throw new IOException("Failed to close.", e);
					}
				} finally {
					writeBuffer = null;
					readBuffer = null;
				}
			}

			@Override
			public long position() {
				return position;
			}

			@Override
			public SftpHandle position(long position) {
				this.position = position;
				return this;
			}

			@Override
			public int read(ByteBuffer buffer) throws SftpException {
				int len = buffer.limit() - buffer.position();
				if (len < 1)
					throw new SftpException(SftpException.OUT_OF_BUFFER_SPACE,
							"Run out of buffer space reading a file.");
				if (readBuffer == null || readBuffer.length != len) {
					readBuffer = new byte[len];
				}
				try {
					try {
						int read = nativeHandle.getSFTPChannel().readFile(nativeHandle.getHandle(),
								new UnsignedInteger64(position), readBuffer, 0, len);
						if (read != -1) {
							buffer.put(readBuffer, 0, read);
							position += read;
						}
						return read;
					} catch (SftpStatusException sftpE) {
						throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
					} catch (com.sshtools.ssh.SshException ioe) {
						throw new SftpException(SftpException.IO_ERROR, String.format("Failed to write to file."), ioe);
					}
				} catch (IOException e) {
					throw new SftpException(SftpException.IO_ERROR, e);
				}
			}

			@Override
			public SftpHandle write(ByteBuffer buffer) throws SftpException {
				int len = buffer.limit() - buffer.position();
				if (writeBuffer == null || writeBuffer.length != len) {
					writeBuffer = new byte[len];
				}
				buffer.get(writeBuffer);
				try {
					nativeHandle.getSFTPChannel().writeFile(nativeHandle.getHandle(), new UnsignedInteger64(position),
							writeBuffer, 0, len);
				} catch (SftpStatusException sftpE) {
					throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
				} catch (com.sshtools.ssh.SshException ioe) {
					throw new SftpException(SftpException.IO_ERROR,
							String.format("Failed to write to file %s.", nativeHandle.getAbsolutePath()), ioe);
				}
				position += len;
				return this;
			}
		};
	}

	@Override
	public void rm(String path, boolean recurse) throws SftpException, SshException {
		try {
			sftpClient.rm(path, true, recurse);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
	public SftpFile lstat(String path) throws SshException {
		try {
			SftpFileAttributes entry = sftpClient.statLink(path);
			return entryToFile(path, entry);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SftpFileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.getSize().longValue(), entry.getModifiedDateTime().getTime(),
				entry.getCreationTime().longValue(), entry.getAccessedTime().longValue(), toInt(entry.getGID()),
				toInt(entry.getUID()), entry.getPermissions().intValue());
	}

	int toInt(String val) {
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
		}
		return 0;
	}

	SftpFile.Type convertType(SftpFileAttributes attrs) {
		if (attrs.isDirectory()) {
			return SftpFile.Type.DIRECTORY;
		} else if (attrs.isLink()) {
			return SftpFile.Type.SYMLINK;
		} else if (attrs.isFile()) {
			return SftpFile.Type.FILE;
		} else if (attrs.isFifo()) {
			return SftpFile.Type.FIFO;
		} else if (attrs.isCharacter()) {
			return SftpFile.Type.CHARACTER;
		} else if (attrs.isBlock()) {
			return SftpFile.Type.BLOCK;
		} else if (attrs.isSocket()) {
			return SftpFile.Type.SOCKET;
		} else {
			return SftpFile.Type.UNKNOWN;
		}
	}

	@Override
	protected InputStream doGet(String path, long filePointer) throws SshException {
		try {
			InputStream in = sftpClient.getInputStream(path, filePointer);
			return new SftpInputStream(in, this, path, in.toString());
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for reading.", e);
		}
	}

	@Override
	protected OutputStream doUpload(final String path) throws SshException {
		try {
			OutputStream out = sftpClient.getOutputStream(path);
			return new SftpOutputStream(out, this, out.toString(), path, 0) ;
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for writing.", e);
		}
	}

	@Override
	public void chmod(final String path, final int permissions) throws SshException {
		try {
			sftpClient.chmod(permissions, path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void doGet(String path, OutputStream out, long filePointer) throws SshException {
		try {
			sftpClient.get(path, out, createProgress(out.toString()), filePointer);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		try {
			sftpClient.put(in, path, createProgress(in.toString()));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to write remote file.", e);
		}
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		try {
			sftpClient.rename(path, newPath);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			SftpFileAttributes stat = sftpClient.stat(path);
			UnsignedInteger64 a = stat.getAccessedTime();
			stat.setTimes(a, new UnsignedInteger64(modtime));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			sftpClient.chown(String.valueOf(uid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			sftpClient.chgrp(String.valueOf(gid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toOperation(sftpClient.copyRemoteDirectory(remotedir, localdir.getAbsolutePath(), recurse, sync, commit,
					createProgress(localdir.getPath())), false, localdir, remotedir);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public SftpOperation upload(File localdir, String remotedir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toOperation(sftpClient.copyLocalDirectory(localdir.getAbsolutePath(), remotedir, recurse, sync, commit,
					createProgress(localdir.getPath())), true, localdir, remotedir);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
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

	@Override
	protected void onEOLPolicyChange(EOLPolicy... eolPolicy) {
		if (sftpClient != null) {
			if (eolPolicy == null || (eolPolicy[0] == null)) {
				sftpClient.setRemoteEOL(defaultRemoteEOL);
			} else {
				List<EOLPolicy> pols = Arrays.asList(eolPolicy);
				if (pols.contains(EOLPolicy.REMOTE_CR))
					sftpClient.setRemoteEOL(SftpClient.EOL_CR);
				else if (pols.contains(EOLPolicy.REMOTE_CR_LF))
					sftpClient.setRemoteEOL(SftpClient.EOL_CRLF);
				else if (pols.contains(EOLPolicy.REMOTE_LF))
					sftpClient.setRemoteEOL(SftpClient.EOL_LF);
				else
					sftpClient.setRemoteEOL(defaultRemoteEOL);
			}
		}
	}

	@Override
	protected void onTransferModeChange(TransferMode transferMode) {
		if (sftpClient != null)
			sftpClient.setTransferMode(transferMode == TransferMode.TEXT ? SftpClient.MODE_TEXT : SftpClient.MODE_BINARY);
	}

	@SuppressWarnings("unchecked")
	private SftpOperation toOperation(com.sshtools.sftp.DirectoryOperation op, boolean up, File localDir, String remoteDir) {
		List<String> updated = new ArrayList<String>();
		for (Object f : op.getUpdatedFiles()) {
			if(up)
				updated.add(toOpPath(localDir, remoteDir, getDefaultPath(), f));
			else
				updated.add(f.toString());
		}
		List<String> unchanged = new ArrayList<String>();
		for (Object f : op.getUnchangedFiles()) {
			if(up)
				unchanged.add(toOpPath(null, remoteDir, getDefaultPath(), f));
			else
				unchanged.add(f.toString());
		}
		List<String> deleted = new ArrayList<String>();
		for (Object f : op.getDeletedFiles()) {
			if(up)
				deleted.add(toOpPath(null, remoteDir, getDefaultPath(), f));
			else
				deleted.add(f.toString());
				
		}
		List<String> created = new ArrayList<String>();
		for (Object f : op.getNewFiles()) {
			if(up)
				created.add(toOpPath(localDir, remoteDir, getDefaultPath(), f));
			else
				created.add(f.toString());
		}
		Set<String> all = new LinkedHashSet<>();
		all.addAll(updated);
		all.addAll(unchanged);
		all.addAll(deleted);
		all.addAll(created);
		Map<String, Exception> errors = new HashMap<String, Exception>();
		for (Map.Entry<Object, Exception> en : ((Map<Object, Exception>) op.getFailedTransfers()).entrySet()) {
			errors.put(en.toString(), en.getValue());
		}
		ArrayList<String> allList = new ArrayList<>(all);
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
				try {
					return op.getTransferSize();
				} catch (SftpStatusException | com.sshtools.ssh.SshException e) {
					throw new IllegalStateException("Failed to get transfer size.", e);
				}
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

	private String toOpPath(File localDir, String remotedir, String defaultPath, Object f) {
		if (f instanceof File) {
			if (localDir != null) {
				return Util.concatenatePaths(remotedir, Util.relativeTo(Util.fixSlashes(((File) f).getPath()), Util.fixSlashes(localDir.getPath())));
			}
			return ((File) f).getPath();
		} else {
			return Util.relativeTo(f.toString(), defaultPath);
		}
	}
}
