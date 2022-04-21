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
package net.sf.sshapi.impl.sshj;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.InMemoryDestFile;
import net.schmizz.sshj.xfer.InMemorySourceFile;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.Util;

/**
 * SSHJ SFTP implementation.
 */
public class SSHJSftpClient extends AbstractSftpClient<SSHJSshClient> {

	private SSHJSshClient client;
	private String defaultPath;
	private SFTPClient sftp;

	/**
	 * Constructor..
	 * 
	 * @param client client
	 */
	public SSHJSftpClient(SSHJSshClient client) {
		super(client);
		this.client = client;
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			sftp.chgrp(path, gid);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to change file group of %s to %d.", path, gid), e);
		}

	}

	@Override
	public void chmod(String path, int permissions) throws SshException {
		try {
			sftp.chmod(path, permissions);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to change file permissions of %s to %d.", path, permissions), e);
		}

	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			sftp.chown(path, uid);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to change file owner of %s to %d.", path, uid), e);
		}

	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws SshException {
		try {
			return new SSHJSftpHandle(sftp.open(path, convertFlags(modes)));
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException ioe) {
			throw new SftpException(SftpException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	@Override
	public String getDefaultPath() {
		return defaultPath;
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		try {
			List<SftpFile> files = new ArrayList<>();
			for (RemoteResourceInfo file : sftp.ls(path)) {
				files.add(toSftpFile(file));
			}
			return files.toArray(new SftpFile[0]);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to create directory %s.", path), e);
		}
	}

	@Override
	public SftpFile lstat(String path) throws SshException {
		try {
			return toSftpFile(path, sftp.lstat(path));
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to read link %s.", path), e);
		}
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			sftp.getSFTPEngine().makeDir(path, permissions(permissions));
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to create directory %s.", path), e);
		}

	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		try {
			sftp.rename(path, newPath);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to rename %s to %s.", path, newPath),
					e);
		}

	}

	@Override
	public void rm(String path) throws SshException {
		try {
			sftp.rm(path);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to remove file %s.", path), e);
		}

	}

	@Override
	public void rmdir(String path) throws SshException {
		try {
			sftp.rmdir(path);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to remove directory %s.", path), e);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			FileAttributes attrs = sftp.stat(path);
			attrs = new FileAttributes(FilePermission.toMask(attrs.getPermissions()), attrs.getSize(), attrs.getUID(),
					attrs.getGID(), attrs.getMode(), attrs.getAtime(), attrs.getMtime(),
					Collections.emptyMap() /* TODO - impossible to copy */);
			sftp.setattr(path, attrs);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR,
					String.format("Failed to set last modified of %s to %d.", path, modtime), e);
		}
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		try {
			return toSftpFile(path, sftp.stat(path));
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to create directory %s.", path), e);
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			String linkpath = Util.getAbsolutePath(path, getDefaultPath());
			String targetpath = Util.getAbsolutePath(target, Util.dirname(linkpath));
			switch(configuration.getSftpSymlinks()) {
			case SshConfiguration.STANDARD_SFTP_SYMLINKS:
				sftp.symlink(linkpath, targetpath);
				break;
			case SshConfiguration.OPENSSH_SFTP_SYMLINKS:
				sftp.symlink(targetpath, linkpath);
				break;
			default:
				if(isOpenSSH()) {
					sftp.symlink(targetpath, linkpath);
				}
				else {
					sftp.symlink(linkpath, targetpath);
				}
				break;
			}
			
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to symlink %s to %s.", path, target),
					e);
		}

	}

	@Override
	protected String doReadLink(String path) throws SshException {
		try {
			return sftp.readlink(path);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to read link %s.", path), e);
		}
	}

	@Override
	protected void onClose() throws SshException {
		try {
			sftp.close();
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to closeSFTP client.", e);
		}
	}

	@Override
	protected void onOpen() throws SshException {
		try {
			sftp = client.getSsh().newSFTPClient();
			defaultPath = sftp.canonicalize(".");
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.GENERAL, "Failed to open SFTP client.", e);
		}
	}

	@Override
	protected void doDownload(String path, OutputStream out) throws SshException {try {
		sftp.get(path, new InMemoryDestFile() {
			@Override
			public OutputStream getOutputStream() throws IOException {
				return out;
			}
		});
	} catch (SFTPException sftpe) {
		throw translateException(sftpe);
	} catch (IOException e) {
		throw new SshException(SshException.IO_ERROR, String.format("Failed to read link %s.", path), e);
	}
	}

	@Override
	protected void doUpload(String path, InputStream in) throws SshException {
		try {
			sftp.put(new InMemorySourceFile() {
				@Override
				public String getName() {
					return Util.basename(path);
				}
				
				@Override
				public long getLength() {
					return 0;
				}
				
				@Override
				public InputStream getInputStream() throws IOException {
					return in;
				}
			},  path);
		} catch (SFTPException sftpe) {
			throw translateException(sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, String.format("Failed to read link %s.", path), e);
		}
	}

	private FileAttributes permissions(int permissions) {
		return new FileAttributes(permissions == -1 ? 0 : permissions, 0, 0, 0, new FileMode(permissions == -1 ? 0 : permissions), 0, 0, Collections.emptyMap());
	}

	private SftpFile toSftpFile(RemoteResourceInfo file) {
		return new SftpFile(toType(file.getAttributes().getType()), file.getPath(), file.getAttributes().getSize(),
				file.getAttributes().getMtime() * 1000, 0, file.getAttributes().getAtime() * 1000,
				file.getAttributes().getGID(), file.getAttributes().getUID(),
				FilePermission.toMask(file.getAttributes().getPermissions()));
	}

	private SftpFile toSftpFile(String path, FileAttributes fileAttributes) {
		return new SftpFile(toType(fileAttributes.getType()), path, fileAttributes.getSize(),
				fileAttributes.getMtime() * 1000, 0, fileAttributes.getAtime() * 1000, fileAttributes.getGID(),
				fileAttributes.getUID(), FilePermission.toMask(fileAttributes.getPermissions()));
	}

	private SftpFile.Type toType(Type type) {
		return SftpFile.Type.fromMask(type.toMask());
	}

	private SftpException translateException(SFTPException sftpe) {
		return new SftpException(sftpe.getStatusCode().getCode());
	}

	private Set<net.schmizz.sshj.sftp.OpenMode> convertFlags(OpenMode[] modes) {
		Set<net.schmizz.sshj.sftp.OpenMode> l = new LinkedHashSet<>();
		for(OpenMode m : modes) {
			l.add(net.schmizz.sshj.sftp.OpenMode.valueOf(m.name().substring(5)));
		}
		return l;
	}
	
	protected final class SSHJSftpHandle implements SftpHandle {
		private final RemoteFile nativeHandle;
		private long position;
		private byte[] readBuffer;
		private byte[] writeBuffer;

		protected SSHJSftpHandle(RemoteFile nativeHandle) {
			this.nativeHandle = nativeHandle;
		}

		@Override
		public void close() throws IOException {
			try {
				try {
					nativeHandle.close();
				} catch (SFTPException e) {
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
					int read = nativeHandle.read(position, readBuffer, 0, len);
					if (read != -1) {
						buffer.put(readBuffer, 0, read);
						position += read;
					}
					return read;
				} catch (SFTPException sftpE) {
					throw translateException(sftpE);
				} catch (IOException ioe) {
					throw new SftpException(SftpException.IO_ERROR, String.format("Failed to read from file %s.", nativeHandle.getPath()), ioe);
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
				nativeHandle.write(position, writeBuffer, 0, len);
			} catch (SFTPException sftpE) {
				throw translateException(sftpE);
			} catch (IOException ioe) {
				throw new SftpException(SftpException.IO_ERROR,
						String.format("Failed to write to file %s.", nativeHandle.getPath()), ioe);
			}
			position += len;
			return this;
		}
	}
}
