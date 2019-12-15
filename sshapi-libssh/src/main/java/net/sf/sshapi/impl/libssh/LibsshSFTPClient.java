package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ochafik.lang.jnaerator.runtime.NativeSize;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_session;
import ssh.sftp_attributes_struct;
import ssh.sftp_dir_struct;
import ssh.sftp_file_struct;
import ssh.sftp_session_struct;

class LibsshSFTPClient extends AbstractSftpClient {

	private SshLibrary library;
	private ssh_session libSshSession;
	private sftp_session_struct sftp;
	private boolean free;

	public LibsshSFTPClient(SshProvider provider, SshConfiguration configuration, SshLibrary library,
			ssh_session libSshSession) {
		super(provider, configuration);
		this.library = library;
		this.libSshSession = libSshSession;
	}

	@Override
	protected void onOpen() throws SshException {
		if (LOG.isDebug())
			LOG.debug("Opening libssh SFTP");
		sftp = library.sftp_new(libSshSession);
		if (sftp == null) {
			throw new SshException(SshException.GENERAL,
					"Failed to open SFTP session. " + library.ssh_get_error(libSshSession.getPointer()));
		}
		if (LOG.isDebug())
			LOG.debug("Opened libssh SFTP, initialising");
		int ret = library.sftp_init(sftp);
		try {
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to initialise SFTP session. " + library.ssh_get_error(libSshSession.getPointer()));
			}
		} catch (SshException sshe) {
			close();
			throw sshe;
		}
	}

	@Override
	protected void onClose() throws SshException {
		LOG.debug("Freeing libssh SFTP");
		if (library.ssh_is_connected(libSshSession) == 1) {
			library.sftp_free(sftp);
			free = true;
		}
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Listing directory {0}", path);

			sftp_dir_struct dir;
			sftp_attributes_struct attributes;

			List<SftpFile> files = new ArrayList<SftpFile>();
			dir = library.sftp_opendir(sftp, path);
			if (dir == null)
				throw new LibsshSFTPException(String.format("Could not open directory. %s", path));
			try {
				while ((attributes = library.sftp_readdir(sftp, dir)) != null) {
					SftpFile f = attributesToFile(path, attributes);
					files.add(f);
				}

				if (library.sftp_dir_eof(dir) == 0) {
					throw new SshException("Failed to list directory " + path + ". "
							+ library.ssh_get_error(libSshSession.getPointer()));
				}
			} finally {
				closeDir(dir);
			}
			dir = null;
			return files.toArray(new SftpFile[0]);
		}
	}

	private void closeDir(sftp_dir_struct dir) throws SshException {
		int ret;
		ret = library.sftp_closedir(dir);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL,
					"Failed to close directory. " + library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	@Override
	public String getDefaultPath() {
		return "/";
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Stat {0}", path);
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			if (attr == null)
				throw new LibsshSFTPException(String.format("Could not find file. %s", path));
			SftpFile f = attributesToFile(path, attr);
			return f;
		}
	}

	@Override
	public SftpFile lstat(String path) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Lstat {0}", path);
			sftp_attributes_struct attr = library.sftp_lstat(sftp, path);
			if (attr == null)
				throw new LibsshSFTPException(String.format("Could not find file. %s", path));
			SftpFile f = attributesToFile(path, attr);
			return f;
		}
	}

	@Override
	public String doReadLink(String path) throws SshException {
		synchronized (sftp) {
			return Util.linkPath(library.sftp_readlink(sftp, path).getString(0), path);
		}
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Mkdir {0}", path);
			int ret = library.sftp_mkdir(sftp, path, permissions);
			if (ret != SshLibrary.SSH_OK) {
				throw new LibsshSFTPException(String.format("Could not create folder. %s", path));
			}
		}
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Symlink {0} {1}", path, target);
			int ret = library.sftp_symlink(sftp, path, target);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not symlink file. %s", path));
		}
	}

	@Override
	public void rm(String path) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Rm {0}", path);
			int ret = library.sftp_unlink(sftp, path);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not remove file. %s", path));
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Rmdir {0}", path);
			int ret = library.sftp_rmdir(sftp, path);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not remove directory. %s", path));
		}
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Rename {0} {1}", path, newPath);
			int ret = library.sftp_rename(sftp, path, newPath);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to remove file. " + library.ssh_get_error(libSshSession.getPointer()));
			}
		}
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws SftpException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("File {0} {1}", path, Arrays.asList(modes));
			sftp_file_struct file;
			file = library.sftp_open(sftp, path, OpenMode.toPOSIX(modes), LibsshClient.S_IRWXU);
			if (file == null)
				throw new LibsshSFTPException(
						String.format("Could not open file. %s. Flags %s", path, Arrays.asList(modes)));

			return new SftpHandle() {

				private long position;

				@Override
				public void close() throws IOException {
					synchronized(libSshSession) {
						if (library.ssh_is_connected(libSshSession) == 1 && !free) {
							if(LOG.isDebug())
								LOG.debug("Closing {0} at position {1}", path, position);
							library.sftp_close(file);
						}
					}
				}

				@Override
				public SftpHandle write(ByteBuffer buffer) throws SftpException {
					if (library.ssh_is_connected(libSshSession) == 0 || free)
						throw new SftpException(SftpException.IO_ERROR, "Disconnected.");
					int len = buffer.limit() - buffer.position();
					int w = (int) library.sftp_write(file, buffer, new NativeSize(len));
					if (w != len) {
						throw new LibsshSFTPException("Failed to write to target.");
					}
					position += w;
					return this;
				}

				@Override
				public int read(ByteBuffer buffer) throws SftpException {
					if (library.ssh_is_connected(libSshSession) == 0 || free)
						return -1;
					int r = (int) library.sftp_read(file, buffer, new NativeSize(buffer.limit()));
					if (r != -1)
						position += r;
					return r;
				}

				@Override
				public SftpHandle position(long position) {
					this.position = position;
					return this;
				}

				@Override
				public long position() {
					return position;
				}
			};
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Stat {0}", path);
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			if (attr == null)
				throw new LibsshSFTPException(String.format("Could not find file. %s", path));
			try {
				attr.mtime = (int) (modtime / 1000l);
				int ret = library.sftp_setstat(sftp, path, attr);
				if (ret != SshLibrary.SSH_OK)
					throw new LibsshSFTPException(String.format("Could not set file last modified. %s", path));
			} finally {
				library.sftp_attributes_free(attr);
			}
		}
	}

	private SftpFile attributesToFile(String path, sftp_attributes_struct attr) {
		String fullPath = attr.name == null ? path : Util.concatenatePaths(path, attr.name.getString(0));
		SftpFile file = new SftpFile(attr.type, fullPath, attr.size, attr.mtime * 1000l, attr.createtime * 1000l,
				attr.atime * 1000l, attr.gid, attr.uid, attr.permissions);
		library.sftp_attributes_free(attr);
		return file;
	}

	@Override
	public void chmod(String path, int permissions) throws SshException {
		synchronized (sftp) {
			if (LOG.isDebug())
				LOG.debug("Chmod {0} {1}", path, permissions);
			int ret = library.sftp_chmod(sftp, path, permissions);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not change file permissions. %s", path));
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		synchronized (sftp) {
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			int ret = library.sftp_chown(sftp, path, uid, attr.gid);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not set file owner. %s", path));
		}
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		synchronized (sftp) {
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			int ret = library.sftp_chown(sftp, path, attr.uid, gid);
			if (ret != SshLibrary.SSH_OK) {
				throw new LibsshSFTPException(String.format("Could not set file group. %s", path));
			}
		}
	}

	@SuppressWarnings("serial")
	class LibsshSFTPException extends SftpException {
		LibsshSFTPException(String message) {
			super(library.sftp_get_error(sftp), String.format("%s. Error %d.", message, library.sftp_get_error(sftp)));
		}
	}
}
