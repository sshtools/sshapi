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
package net.sf.sshapi.impl.maverick16;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.maverick.sftp.FileTransferProgress;
import com.maverick.sftp.SftpFileAttributes;
import com.maverick.sftp.SftpStatusException;
import com.maverick.sftp.SftpSubsystemChannel;
import com.maverick.ssh.SshClient;
import com.maverick.util.UnsignedInteger64;
import com.sshtools.sftp.DirectoryOperation;
import com.sshtools.sftp.SftpClient;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.sftp.SftpInputStream;
import net.sf.sshapi.sftp.SftpOperation;
import net.sf.sshapi.sftp.SftpOutputStream;
import net.sf.sshapi.util.Util;

class MaverickSftpClient extends AbstractSftpClient {
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
			} catch (SftpStatusException | com.maverick.ssh.SshException e) {
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

		@Override
		public String toString() {
			return "MaverickSftpOperation [deleted=" + deleted + ", op=" + op + ", errors=" + errors + ", created="
					+ created + ", allList=" + allList + ", unchanged=" + unchanged + ", updated=" + updated + "]";
		}
	}

	private final SshClient client;
	private int defaultLocalEOL;
	private int defaultRemoteEOL;
	private String home;
	private SftpClient sftpClient;
	private MaverickSshClient sshapiClient;

	MaverickSftpClient(MaverickSshClient client) {
		super(client.getProvider(), client.getConfiguration());
		this.client = client.getNativeClient();
		this.sshapiClient = client;
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			sftpClient.chgrp(String.valueOf(gid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public void chmod(final String path, final int permissions) throws SshException {
		try {
			sftpClient.chmod(permissions, path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			sftpClient.chown(String.valueOf(uid), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(e.getLocalizedMessage());
		}
	}

	@Override
	public SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit)
			throws SshException {
		try {
			return toDownOperation(localdir, remotedir, sftpClient.copyRemoteDirectory(remotedir, localdir.getAbsolutePath(), recurse, sync,
					commit, createProgress(localdir.getPath(), 0)));
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
			return toUpOperation(localdir, remotedir, sftpClient.copyLocalDirectory(localdir.getAbsolutePath(), remotedir, recurse, sync,
					commit, createProgress(localdir.getPath(), 0)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	private FileTransferProgress createProgress(String target, long offset) {
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
				long v = progress - offset;
				if (v > 0)
					fireFileTransferProgressed(path, target, progress);
			}

			@Override
			public void started(long length, String path) {
				this.path = path;
				fireFileTransferStarted(path, target, Math.max(0, length - offset));
			}
		};
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws SshException {
		try {
			return createHandle(sftpClient.getSubsystemChannel().openFile(path, OpenMode.toFlags(modes)));
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (com.maverick.ssh.SshException ioe) {
			throw new SftpException(SftpException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	@Override
	public String getDefaultPath() {
		return home;
	}

	@Override
	public int getSftpVersion() {
		return sftpClient.getSubsystemChannel().getServerVersion();
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		try {
			com.maverick.sftp.SftpFile[] entries = sftpClient.ls(path);
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
	public Iterable<SftpFile> list(String path) throws SshException {
		return new Iterable<SftpFile>() {
			@Override
			public Iterator<SftpFile> iterator() {
				try {
					Iterator<com.maverick.sftp.SftpFile> entries = sftpClient.lsIterator(path);
					return new Iterator<SftpFile>() {
						@Override
						public boolean hasNext() {
							return entries.hasNext();
						}

						@Override
						public SftpFile next() {
							try {
								return entryToFile(path, entries.next());
							} catch (Exception e) {
								throw new IllegalStateException("Failed to iterate directory.", e);
							}
						}
					};
				} catch (Exception e) {
					throw new IllegalStateException("Failed to iterate directory.", e);
				}
			}
		};
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
		} catch (com.maverick.ssh.SshException sshe) {
			throw new SshException(SshException.GENERAL, sshe);
		}
	}

	@Override
	public void onOpen() throws SshException {
		try {
			// int sftpMode = SftpClientFactory.class.getField(
			// sshapiClient.getConfiguration().getProperties().getProperty(MaverickSshProvider.CFG_SFTP_MODE,
			// "SFTP_ALL_MODES"))
			// .getInt(null);
			int sftpMaxVersion = Integer.parseInt(sshapiClient.getConfiguration().getProperties().getProperty(
					MaverickSshProvider.CFG_SFTP_MAX_VERSION, String.valueOf(SftpSubsystemChannel.MAX_VERSION)));
			sftpClient = new SftpClient(client, sftpMaxVersion);
			home = sftpClient.pwd();
			if (sftpClient.getSubsystemChannel().getServerVersion() > 3)
				defaultRemoteEOL = sftpClient.getRemoteEOL();
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
			UnsignedInteger64 a = stat.getAccessedTime();
			stat.setTimes(a, new UnsignedInteger64(modtime));
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
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected String doReadLink(String path) throws SshException {
		try {
			return Util.linkPath(sftpClient.getAbsolutePath(path), path);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to read symlink.", e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			sftpClient.getSubsystemChannel().createSymbolicLink(path, target);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create symlink.", e);
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
	protected boolean isUseRawSFTP(long offset) {
		/*
		 * Maverick natively supports the main transfer methods regardless of it offset
		 * is used
		 */
		return false;
	}

	@Override
	public void resumeGet(String path, File destination) throws SshException {
		try {
			sftpClient.get(path, destination.getAbsolutePath(), createProgress(destination.getAbsolutePath(), 0), true);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to resume get.", e);
		}
	}

	@Override
	public void resumePut(File source, String path) throws SshException {
		try {
			sftpClient.put(source.getAbsolutePath(), path, createProgress(source.getAbsolutePath(), 0), true);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to resume put.", e);
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
	protected void doGet(String path, OutputStream out, long filePointer) throws SshException {
		try {
			sftpClient.get(path, out, createProgress(out.toString(), 0), filePointer);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to create directory.", e);
		}
	}

	@Override
	protected void doPut(String path, InputStream in, long offset) throws SshException {
		try {
			sftpClient.put(in, path, createProgress(in.toString(), offset), offset);
		} catch (SftpStatusException sftpE) {
			throw new SftpException(sftpE.getStatus(), sftpE.getLocalizedMessage());
		} catch (Exception e) {
			throw new SshException("Failed to write remote file.", e);
		}
	}

	@Override
	protected OutputStream doUpload(String path) throws SshException {
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
	protected void onEOLPolicyChange(EOLPolicy... eolPolicy) {
		if (sftpClient != null) {
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

	int toInt(String val) {
		try {
			return Integer.parseInt(val);
		} catch (Exception e) {
		}
		return 0;
	}

	private SftpHandle createHandle(com.maverick.sftp.SftpFile nativeHandle) {
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
					} catch (com.maverick.ssh.SshException e) {
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
					} catch (com.maverick.ssh.SshException ioe) {
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
				} catch (com.maverick.ssh.SshException ioe) {
					throw new SftpException(SftpException.IO_ERROR,
							String.format("Failed to write to file %s.", nativeHandle.getAbsolutePath()), ioe);
				}
				position += len;
				return this;
			}
		};
	}

	private SftpFile entryToFile(String path, com.maverick.sftp.SftpFile entry)
			throws SftpStatusException, com.maverick.ssh.SshException {
		String fullPath = Util.concatenatePaths(path, entry.getFilename());
		SftpFileAttributes attr = entry.getAttributes();
		// Bug in Maverick <= 1.6.6
		Date accessedDateTime;
		try {
			accessedDateTime = attr.getAccessedDateTime();
		} catch (NullPointerException npe) {
			accessedDateTime = new Date(attr.getAccessedTime().longValue() * 1000);
		}
		SftpFile file = new SftpFile(convertType(attr), fullPath, attr.getSize().longValue(),
				attr.getModifiedDateTime().getTime(), attr.getCreationDateTime().getTime(), accessedDateTime.getTime(),
				toInt(attr.getGID()), toInt(attr.getUID()), attr.getPermissions().intValue());
		return file;
	}

	private SftpFile entryToFile(String path, SftpFileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.getSize().longValue(),
				entry.getModifiedDateTime().getTime(), entry.getCreationTime().longValue(),
				entry.getAccessedTime().longValue(), toInt(entry.getGID()), toInt(entry.getUID()),
				entry.getPermissions().intValue());
	}

	@SuppressWarnings("unchecked")
	private SftpOperation toUpOperation(File localDir, String remoteDir, com.sshtools.sftp.DirectoryOperation op) {
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
	private SftpOperation toDownOperation(File localDir, String remoteDir, com.sshtools.sftp.DirectoryOperation op) {
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
