package net.sf.sshapi.impl.libssh;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_error_types_e;
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

	public SftpFile[] ls(String path) throws SshException, FileNotFoundException {
		synchronized (sftp) {
			sftp_dir_struct dir;
			sftp_attributes_struct attributes;

			List<SftpFile> files = new ArrayList<SftpFile>();
			dir = library.sftp_opendir(sftp, path);
			if (dir == null) {
				if(translateError("Failed to open directory", path))
					throw new FileNotFoundException(String.format("Could not open directory. %s", path));
			}
			try {
				while ((attributes = library.sftp_readdir(sftp, dir)) != null) {
					SftpFile f = attributesToFile(path, attributes);
					files.add(f);
					System.out.println("Found " + f);
				}

				System.out.println("Checking EOF");
				if (library.sftp_dir_eof(dir) == 0) {
					throw new SshException(
							"Failed to list directory " + path + ". " + library.ssh_get_error(libSshSession.getPointer()));
				}
			} finally {
				closeDir(dir);
			}
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
			throw new SshException(SshException.GENERAL, "Failed to close directory. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public String getDefaultPath() throws SshException {
		return "/";
	}

	public SftpFile stat(String path) throws SshException, FileNotFoundException {
		synchronized (sftp) {
			System.out.println("Stat '" + path + "'");
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			if (attr == null) {
				if (translateError("Failed to get file attributes", path))
					throw new FileNotFoundException(String.format("Could not find file. %s", path));
			}
			SftpFile f = attributesToFile(path, attr);
			return f;
		}
	}

	public void mkdir(String path, int permissions) throws SshException, FileSystemException {
		synchronized (sftp) {
			int ret = library.sftp_mkdir(sftp, path, permissions);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to create directory.", path))
					throw new FileSystemException(String.format("Could not create folder. %s", path));
			}
		}
	}

	public void rm(String path) throws SshException, FileSystemException {
		synchronized (sftp) {
			int ret = library.sftp_unlink(sftp, path);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to remove file", path))
					throw new FileSystemException(String.format("Could not remove file. %s", path));
			}
		}
	}

	public void rmdir(String path) throws SshException, FileSystemException {
		synchronized (sftp) {
			int ret = library.sftp_rmdir(sftp, path);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to remove directory", path))
					throw new FileSystemException(String.format("Could not remove directory. %s", path));
			}
		}
	}

	public void rename(String path, String newPath) throws SshException {
		synchronized (sftp) {
			int ret = library.sftp_rename(sftp, path, newPath);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to remove file. " + library.ssh_get_error(libSshSession.getPointer()));
			}
		}
	}

	public void get(String path, OutputStream out) throws SshException, FileNotFoundException {
		synchronized (sftp) {
			sftp_file_struct file;
			file = library.sftp_open(sftp, path, LibsshClient.O_RDONLY, 0);
			if (file == null) {
				if (translateError("Failed to open file", path))
					throw new FileNotFoundException(String.format("Could not open file. %s", path));
			}
			try {
				Memory buf = new Memory(LibsshClient.SFTP_BUFFER_SIZE);
				int nbytes = library.sftp_read(file, buf, new NativeSize(buf.size()));
				while (nbytes > 0) {
					try {
						out.write(buf.getByteArray(0, nbytes));
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

	public void put(String path, InputStream in, int permissions) throws SshException {
		throw new UnsupportedOperationException();
	}

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

	public void chmod(String path, int permissions) throws SshException, FileSystemException {
		synchronized (sftp) {
			int ret = library.sftp_chmod(sftp, path, permissions);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to change file permissions", path))
					throw new FileSystemException(String.format("Could not change file permissions. %s", path));
			}
		}
	}

	public void chown(String path, int uid) throws SshException, FileSystemException {
		synchronized (sftp) {
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			int ret = library.sftp_chown(sftp, path, uid, attr.gid);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to set file owner", path))
					throw new FileSystemException(String.format("Could not set file owner. %s", path));
			}
		}
	}

	public void chgrp(String path, int gid) throws SshException, FileSystemException {
		synchronized (sftp) {
			sftp_attributes_struct attr = library.sftp_stat(sftp, path);
			int ret = library.sftp_chown(sftp, path, attr.uid, gid);
			if (ret != SshLibrary.SSH_OK) {
				if (translateError("Failed to set file group", path))
					throw new FileSystemException(String.format("Could not set file group. %s", path));
			}
		}
	}

	private boolean translateError(String message, String path) throws SshException {
		int code = library.ssh_get_error_code(libSshSession.getPointer());
		if (code == ssh_error_types_e.SSH_REQUEST_DENIED)
			return true;
		throw new SshException(SshException.GENERAL,
				String.format("%s. Error %d. %s", message, code, library.ssh_get_error(libSshSession.getPointer())));
	}
}
