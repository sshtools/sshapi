package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
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

	public LibsshSFTPClient(SshLibrary library, ssh_session libSshSession) {
		this.library = library;
		this.libSshSession = libSshSession;
	}

	@Override
	protected void onOpen() throws SshException {
		sftp = library.sftp_new(libSshSession);
		if (sftp == null) {
			throw new SshException(SshException.GENERAL,
					"Failed to open SFTP session. " + library.ssh_get_error(libSshSession.getPointer()));
		}
		int ret = library.sftp_init(sftp);
		try {
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to initialise SFTP session. " + library.ssh_get_error(libSshSession.getPointer()));
			}
		} catch (SshException sshe) {
			library.sftp_free(sftp);
			throw sshe;
		}
	}

	@Override
	protected void onClose() throws SshException {
		library.sftp_free(sftp);
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		synchronized (sftp) {
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
					System.out.println("Found " + f);
				}

				System.out.println("Checking EOF");
				if (library.sftp_dir_eof(dir) == 0) {
					throw new SshException("Failed to list directory " + path + ". "
							+ library.ssh_get_error(libSshSession.getPointer()));
				}
			} finally {
				closeDir(dir);
			}
			dir = null;
			System.out.println("Done listing");
			return files.toArray(new SftpFile[0]);
		}
	}

	private void closeDir(sftp_dir_struct dir) throws SshException {
		int ret;
		System.out.println("Closing dir");
		ret = library.sftp_closedir(dir);
		System.out.println("Closed dir with status " + ret);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL,
					"Failed to close directory. " + library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	@Override
	public String getDefaultPath() throws SshException {
		return "/";
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		synchronized (sftp) {
			System.out.println("Stat '" + path + "'");
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			if (attr == null)
				throw new LibsshSFTPException(String.format("Could not find file. %s", path));
			SftpFile f = attributesToFile(path, attr);
			return f;
		}
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		synchronized (sftp) {
			int ret = library.sftp_mkdir(sftp, path, permissions);
			if (ret != SshLibrary.SSH_OK) {
				throw new LibsshSFTPException(String.format("Could not create folder. %s", path));
			}
		}
	}

	@Override
	public void rm(String path) throws SshException {
		synchronized (sftp) {
			int ret = library.sftp_unlink(sftp, path);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not remove file. %s", path));
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		synchronized (sftp) {
			int ret = library.sftp_rmdir(sftp, path);
			if (ret != SshLibrary.SSH_OK)
				throw new LibsshSFTPException(String.format("Could not remove directory. %s", path));
		}
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		synchronized (sftp) {
			int ret = library.sftp_rename(sftp, path, newPath);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to remove file. " + library.ssh_get_error(libSshSession.getPointer()));
			}
		}
	}

	@Override
	public void get(String path, OutputStream out) throws SshException {
		synchronized (sftp) {
			sftp_file_struct file;
			file = library.sftp_open(sftp, path, LibsshClient.O_RDONLY, 0);
			if (file == null)
				throw new LibsshSFTPException(String.format("Could not open file. %s", path));
			try {
				Memory buf = new Memory(LibsshClient.SFTP_BUFFER_SIZE * 4);
				long nbytes = library.sftp_read(file, buf, new NativeSize(buf.size()));
				while (nbytes > 0) {
					try {
						out.write(buf.getByteArray(0, (int) nbytes));
					} catch (IOException e) {
						throw new SshException(SshException.GENERAL, "Failed to write to target stream.", e);
					}
					nbytes = library.sftp_read(file, buf, new NativeSize(buf.size()));
				}

				if (nbytes < 0) {
					throw new SshException(SshException.GENERAL,
							"Failed to read remote file. " + library.ssh_get_error(libSshSession.getPointer()));
				}
			} finally {
				library.sftp_close(file);
			}
		}
	}

	@Override
	public void put(String path, InputStream in, int permissions) throws SshException {
		synchronized (sftp) {
			sftp_file_struct file = library.sftp_open(sftp, path, LibsshClient.O_WRONLY | LibsshClient.O_CREAT | LibsshClient.O_TRUNC, permissions);
			if (file == null)
				throw new LibsshSFTPException(String.format("Could not open file for writing. %s. ", path));
			try {
				Memory buf = new Memory(LibsshClient.SFTP_BUFFER_SIZE * 4);
				byte[] nb = new byte[64];
				int r;
				while( ( r = in.read(nb)) != -1) {
					buf.write(0, nb, 0, r);
					int w = (int) library.sftp_write(file, buf, new NativeSize(r));
					if(w != r) {
						throw new LibsshSFTPException("Failed to write to target.");
					}
				}
			} catch (IOException e) {
				throw new SshException(SshException.GENERAL, "Failed to read from source.", e);
			} finally {
				library.sftp_close(file);
			}
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		throw new UnsupportedOperationException();
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
