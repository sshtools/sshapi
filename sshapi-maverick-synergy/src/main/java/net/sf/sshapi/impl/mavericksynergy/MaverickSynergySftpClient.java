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
package net.sf.sshapi.impl.mavericksynergy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sshtools.client.sftp.DirectoryOperation;
import com.sshtools.client.sftp.SftpClient;
import com.sshtools.client.tasks.FileTransferProgress;
import com.sshtools.common.files.AbstractFile;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.sftp.PosixPermissions.PosixPermissionsBuilder;
import com.sshtools.common.sftp.SftpFileAttributes;
import com.sshtools.common.sftp.SftpFileAttributes.SftpFileAttributesBuilder;
import com.sshtools.common.sftp.SftpStatusException;
import com.sshtools.common.util.EOLProcessor;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.sftp.SftpInputStream;
import net.sf.sshapi.sftp.SftpOperation;
import net.sf.sshapi.sftp.SftpOutputStream;
import net.sf.sshapi.util.SftpDirectoryStream;
import net.sf.sshapi.util.Util;

class MaverickSynergySftpClient extends AbstractSftpClient<MaverickSynergySshClient> {
	private int defaultLocalEOL;
	private int defaultRemoteEOL;
	private String home;
	private SftpClient sftpClient;

	MaverickSynergySftpClient(MaverickSynergySshClient client) {
		super(client);
		defaultLocalEOL = EOLProcessor.TEXT_SYSTEM;
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			sftpClient.chgrp(String.valueOf(gid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public void chmod(final String path, final int permissions) throws SshException {
		try {
			sftpClient.chmod(PosixPermissionsBuilder.create().fromBitmask(Integer.toUnsignedLong(permissions)).build(), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			sftpClient.chown(String.valueOf(uid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toDownOperation(localdir, remotedir, sftpClient.getRemoteDirectory(remotedir, localdir.getAbsolutePath(), recurse, sync,
					commit, createProgress(localdir.toString(), 0, -1)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws net.sf.sshapi.sftp.SftpException {
		try {
			return new MavericySynergySftpHandle(sftpClient.openFile(path, OpenMode.toFlags(modes)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.sshtools.common.ssh.SshException ioe) {
			throw new SftpException(SshException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	@Override
	public DirectoryStream<SftpFile> directory(String path, DirectoryStream.Filter<SftpFile> filter) throws net.sf.sshapi.sftp.SftpException {
		return new SftpDirectoryStream<MavericySynergySftpHandle, com.sshtools.client.sftp.SftpFile>(path, filter) {
			@Override
			public List<com.sshtools.client.sftp.SftpFile> readDirectory(MavericySynergySftpHandle handle) {
				try {
					return sftpClient.readDirectory(handle.nativeHandle);
				} catch (SftpStatusException | com.sshtools.common.ssh.SshException e) {
					throw new IllegalStateException("Failed to read directory.", e);
				}
			}

			@Override
			public MavericySynergySftpHandle createDirectoryHandle(String path) {
				try {
					return new MavericySynergySftpHandle(sftpClient.openDirectory(path));
				} catch (SftpStatusException | com.sshtools.common.ssh.SshException e) {
					throw new IllegalStateException("Failed to open directory.", e);
				}
			}

			@Override
			public SftpFile nativeToFile(String path, com.sshtools.client.sftp.SftpFile nativeFile) {
				try {
					return entryToFile(path, nativeFile);
				} catch (SftpStatusException | com.sshtools.common.ssh.SshException e) {
					throw new IllegalStateException("Failed to convert native file.", e);
				}
			}
		};
	}

	@Override
	public String getDefaultPath() {
		return home;
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		try {
			com.sshtools.client.sftp.SftpFile[] entries = sftpClient.ls(path);
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
	public void onClose() throws SshException {
		try {
			sftpClient.exit();
		} catch (com.sshtools.common.ssh.SshException sshe) {
			throw new SshException(SshException.GENERAL, sshe);
		}
	}

	@Override
	public void onOpen() throws SshException {
		try {
			sftpClient = new SftpClient.SftpClientBuilder().build();
			if(client.getConfiguration().getSftpBlockSize() != 0) {
				sftpClient.setBlockSize((int)client.getConfiguration().getSftpBlockSize());
			}
			if (getSftpVersion() > 3) {
				defaultRemoteEOL = sftpClient.getRemoteEOL();
				if (eolPolicy != null)
					onEOLPolicyChange(eolPolicy);
				if (transferMode != null)
					onTransferModeChange(transferMode);
			}
			home = sftpClient.pwd();
		} catch (com.sshtools.common.ssh.SshException | IOException e) {
			throw new SshException(SshException.GENERAL, "Failed to start SFTP.", e);
		} catch (PermissionDeniedException e) {
			throw new SshException(SshException.PERMISSION_DENIED, "Failed to open SFTP nativeClient.", e);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
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
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			SftpFileAttributes stat = sftpClient.stat(path);
			SftpFileAttributes newStat = SftpFileAttributesBuilder.create().
					withFileAttributes(stat).
					withLastModifiedTime(modtime).
					build();
			sftpClient.setAttributes(path, newStat);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
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
			throw new SshException("Failed to stat directory.", e);
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

	@Override
	protected String doReadLink(String path) throws SshException {
		try {
			return Util.linkPath(sftpClient.getSymbolicLinkTarget(path), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to read link.", e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			/*
			 * Avoid sftpClient.symlink() because it makes paths relative to the home
			 * directory
			 */
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
	public void link(String path, String target) throws SshException {
		try {
			String linkpath = Util.getAbsolutePath(path, getDefaultPath());
			String targetpath = Util.getAbsolutePath(target, Util.dirname(linkpath));
			sftpClient.getSubsystemChannel().createLink(targetpath, linkpath, false);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create symlink.", e);
		}
	}
	
	@Override
	public SftpOperation upload(File localdir, String remotedir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toUpOperation(localdir, remotedir, sftpClient.putLocalDirectory(localdir.getAbsolutePath(), remotedir, recurse, sync,
					commit, createProgress(localdir.getPath(), 0, -1)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	public void resumeGet(String path, File destination) throws SshException {
		try {
			sftpClient.get(path, destination.getAbsolutePath(), createProgress(destination.getAbsolutePath(), 0, 0), true);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to resume get.", e);
		}
	}

	@Override
	public void resumePut(File source, String path) throws SshException {
		try {
			sftpClient.put(source.getAbsolutePath(), path, createProgress(source.getAbsolutePath(), 0, -1), true);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to resume put.", e);
		}
	}

	@Override
	protected boolean isUseRawSFTP(long offset) {
		return false;
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
	protected void doPut(String path, InputStream in, long offset) throws SshException {
		try {
			sftpClient.put(in, path, createProgress(in.toString(), offset, 0), offset, -1);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to write remote file.", e);
		}
	}

	@Override
	protected void doGet(String path, OutputStream out, long filePointer) throws SshException {
		try {
			sftpClient.get(path, out, createProgress(out.toString(), 0, 0), filePointer);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected OutputStream doUpload(final String path) throws SshException {
		try {
			OutputStream out = sftpClient.getOutputStream(path);
			return new SftpOutputStream(out, this, path, out.toString());
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to open remote file for writing.", e);
		}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		try {
			sftpClient.put(in, path, createProgress(in.toString(), 0, 0));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to write remote file.", e);
		}
	}

	@Override
	protected void onEOLPolicyChange(EOLPolicy... eolPolicy) {
		if (sftpClient != null) {
			if (getSftpVersion() > 3) {
				if (eolPolicy == null || (eolPolicy[0] == null)) {
					sftpClient.setForceRemoteEOL(false);
					sftpClient.setRemoteEOL(defaultRemoteEOL);
					sftpClient.setLocalEOL(defaultLocalEOL);
				} else {
					List<EOLPolicy> pols = Arrays.asList(eolPolicy);
					sftpClient.setForceRemoteEOL(pols.contains(EOLPolicy.FORCE_REMOTE));
					if (pols.contains(EOLPolicy.REMOTE_CR))
						sftpClient.setRemoteEOL(SftpClient.EOL_CR);
					else if (pols.contains(EOLPolicy.REMOTE_CR_LF))
						sftpClient.setRemoteEOL(SftpClient.EOL_CRLF);
					else if (pols.contains(EOLPolicy.REMOTE_LF))
						sftpClient.setRemoteEOL(SftpClient.EOL_LF);
					else
						sftpClient.setRemoteEOL(defaultRemoteEOL);
					if (pols.contains(EOLPolicy.LOCAL_CR))
						sftpClient.setLocalEOL(SftpClient.EOL_CR);
					else if (pols.contains(EOLPolicy.REMOTE_CR_LF))
						sftpClient.setLocalEOL(SftpClient.EOL_CRLF);
					else if (pols.contains(EOLPolicy.REMOTE_LF))
						sftpClient.setLocalEOL(SftpClient.EOL_LF);
					else
						sftpClient.setLocalEOL(defaultLocalEOL);
				}
			} else {
				SshConfiguration.getLogger().warn("EOL settings are not available for version {0} of SFTP",
						getSftpVersion());
			}
		}
	}

	@Override
	protected void onTransferModeChange(TransferMode transferMode) {
		if (sftpClient != null)
			sftpClient
					.setTransferMode(transferMode == TransferMode.TEXT ? SftpClient.MODE_TEXT : SftpClient.MODE_BINARY);
	}

	long convertIntDate(Integer date) {
		return date == null ? 0 : date.intValue() * 1000l;
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

	int toInt(String val) {
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
		}
		return 0;
	}

	private FileTransferProgress createProgress(String target, long offset, long initFirst) {
		return new FileTransferProgress() {
			private String path;
			private long first = initFirst;

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
				if (first == -1) {
					first = progress;
					if(progress > 0) {
						return;
					}
				}
				long v = progress - offset - first;
				if (v > 0)
					fireFileTransferProgressed(path, target, v);
			}

			@Override
			public void started(long length, String path) {
				this.path = path;
				fireFileTransferStarted(path, target, Math.max(0, length - offset));
			}
		};
	}

	private SftpFile entryToFile(String path, com.sshtools.client.sftp.SftpFile entry)
			throws SftpStatusException, com.sshtools.common.ssh.SshException {
		return entryToFile(Util.concatenatePaths(path, entry.getFilename()), entry.attributes());
	}

	private SftpFile entryToFile(String fullPath, SftpFileAttributes attr) {

		return new SftpFile(convertType(attr), fullPath, attr.size().longValue(),
				attr.lastModifiedTimeOr().map(FileTime::toInstant).map(Instant::toEpochMilli).orElse(0l), 
				attr.createTimeOr().map(FileTime::toInstant).map(Instant::toEpochMilli).orElse(0l),  
				attr.lastAccessTimeOr().map(FileTime::toInstant).map(Instant::toEpochMilli).orElse(0l),
				attr.gidOr().orElse(0), attr.uidOr().orElse(0),
				attr.permissions().asInt());
	}

	@SuppressWarnings("unchecked")
	private SftpOperation toUpOperation(File localDir, String remoteDir, DirectoryOperation op) {
		List<String> updated = new ArrayList<String>();
		String defaultPath = getDefaultPath();
		for (Object f : op.getUpdatedFiles())
			updated.add(toOpPath(localDir, remoteDir, defaultPath, f));
		List<String> unchanged = new ArrayList<String>();
		for (Object f : op.getUnchangedFiles())
			unchanged.add(toOpPath(null, remoteDir, defaultPath, f));
		List<String> deleted = new ArrayList<String>();
		for (Object f : op.getDeletedFiles())
			deleted.add(toOpPath(null, remoteDir, defaultPath, f));
		List<String> created = new ArrayList<String>();
		for (Object f : op.getNewFiles())
			created.add(toOpPath(localDir, remoteDir, defaultPath, f));
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
		return new MaverickSftpOperation(deleted, op, errors, created, allList, unchanged, updated);
	}

	@SuppressWarnings("unchecked")
	private SftpOperation toDownOperation(File localDir, String remoteDir, DirectoryOperation op) {
		List<String> updated = new ArrayList<String>();
		String defaultPath = getDefaultPath();
		for (Object f : op.getUpdatedFiles())
			updated.add(toOpPath(null, remoteDir, defaultPath, f));
		List<String> unchanged = new ArrayList<String>();
		for (Object f : op.getUnchangedFiles())
			unchanged.add(toOpPath(null, remoteDir, defaultPath, f));
		List<String> deleted = new ArrayList<String>();
		for (Object f : op.getDeletedFiles())
			deleted.add(toOpPath(null, remoteDir, defaultPath, f));
		List<String> created = new ArrayList<String>();
		for (Object f : op.getNewFiles())
			created.add(toOpPath(localDir, remoteDir, defaultPath, f));
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
		return new MaverickSftpOperation(deleted, op, errors, created, allList, unchanged, updated);
	}
	
	protected final class MavericySynergySftpHandle implements SftpHandle {
		private final com.sshtools.client.sftp.SftpHandle nativeHandle;
		private long position;
		private byte[] readBuffer;
		private byte[] writeBuffer;

		protected MavericySynergySftpHandle(com.sshtools.client.sftp.SftpHandle nativeHandle) {
			this.nativeHandle = nativeHandle;
		}

		@Override
		public void close() throws IOException {
			try {
				nativeHandle.close();
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
					int read = nativeHandle.read(position, readBuffer, 0, len);
					if (read != -1) {
						buffer.put(readBuffer, 0, read);
						position += read;
					}
					return read;
				} catch (SftpStatusException sftpE) {
					throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
				} catch (com.sshtools.common.ssh.SshException ioe) {
					throw new SftpException(SshException.IO_ERROR, String.format("Failed to write to file."), ioe);
				}
			} catch (IOException e) {
				throw new SftpException(SshException.IO_ERROR, e);
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
				nativeHandle.write(position, writeBuffer, 0, len);
			} catch (SftpStatusException sftpE) {
				throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
			} catch (com.sshtools.common.ssh.SshException ioe) {
				throw new SftpException(SshException.IO_ERROR, String.format("Failed to write to file."), ioe);
			}
			position += len;
			return this;
		}
	}

	protected final class MaverickSftpOperation implements SftpOperation {
		private final List<String> deleted;
		private final DirectoryOperation op;
		private final Map<String, Exception> errors;
		private final List<String> created;
		private final ArrayList<String> allList;
		private final List<String> unchanged;
		private final List<String> updated;

		protected MaverickSftpOperation(List<String> deleted, DirectoryOperation op, Map<String, Exception> errors,
				List<String> created, ArrayList<String> allList, List<String> unchanged, List<String> updated) {
			this.deleted = deleted;
			this.op = op;
			this.errors = errors;
			this.created = created;
			this.allList = allList;
			this.unchanged = unchanged;
			this.updated = updated;
		}

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
			} catch (SftpStatusException | com.sshtools.common.ssh.SshException | IOException | PermissionDeniedException e) {
				return 0;
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

		@Override
		public String toString() {
			return "MaverickSftpOperation [deleted=" + deleted + ", op=" + op + ", errors=" + errors + ", created="
					+ created + ", allList=" + allList + ", unchanged=" + unchanged + ", updated=" + updated + "]";
		}
	}

	private String toOpPath(File localDir, String remotedir, String defaultPath, Object f) {
		if(f instanceof AbstractFile) {
			AbstractFile file = (AbstractFile)f;
			try {
				if (localDir != null) {
					return Util.concatenatePaths(remotedir, Util.relativeTo(Util.fixSlashes(file.getAbsolutePath()), Util.fixSlashes(localDir.getPath())));
				}
				return file.getAbsolutePath();
			} catch (Exception e) {
				return file.getName(); 
			}
		}
		else if (f instanceof File) {
			if (localDir != null) {
				return Util.concatenatePaths(remotedir, Util.relativeTo(Util.fixSlashes(((File) f).getPath()), Util.fixSlashes(localDir.getPath())));
			}
			return ((File) f).getPath();
		} else {
			return Util.relativeTo(f.toString(), defaultPath);
		}
	}
}
