package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_session;
import ssh.sftp_attributes_struct;
import ssh.sftp_dir_struct;
import ssh.sftp_file_struct;
import ssh.sftp_session_struct;

import com.ochafik.lang.jnaerator.runtime.NativeSize;
import com.sun.jna.Memory;

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
			throw new SshException(SshException.GENERAL, "Failed to open SFTP session. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
		int ret = library.sftp_init(sftp);
		try {
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL, "Failed to initialise SFTP session. "
					+ library.ssh_get_error(libSshSession.getPointer()));
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

	public SftpFile[] ls(String path) throws SshException {
		sftp_dir_struct dir;
		sftp_attributes_struct attributes;
		int ret = 0;

		List files = new ArrayList();
		dir = library.sftp_opendir(sftp, path);
		try {
			if (dir == null) {
				throw new SshException("Failed to open directory " + path + ". "
					+ library.ssh_get_error(libSshSession.getPointer()));
			}
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
		System.out.println("Done listing");
		return (SftpFile[]) files.toArray(new SftpFile[0]);
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

	public SftpFile stat(String path) throws SshException {
		System.out.println("Stat '" + path + "'");
		sftp_attributes_struct attr = library.sftp_stat(sftp, path);
		if (attr == null) {
			throw new SshException(SshException.GENERAL, "Failed to get file attributes. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
		SftpFile f = attributesToFile(path, attr);
		return f;
	}

	public void mkdir(String path, int permissions) throws SshException {
		int ret = library.sftp_mkdir(sftp, path, permissions);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to create directory. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public void rm(String path) throws SshException {
		int ret = library.sftp_unlink(sftp, path);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to remove file. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public void rmdir(String path) throws SshException {
		int ret = library.sftp_rmdir(sftp, path);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to remove directory. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public void rename(String path, String newPath) throws SshException {
		int ret = library.sftp_rename(sftp, path, newPath);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to remove file. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public void get(String path, OutputStream out) throws SshException {
		sftp_file_struct file;
		file = library.sftp_open(sftp, path, LibsshClient.O_RDONLY, 0);
		try {
			if (file == null) {
				throw new SshException(SshException.GENERAL, "Failed to open file. "
					+ library.ssh_get_error(libSshSession.getPointer()));
			}
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
				throw new SshException(SshException.GENERAL, "Failed to read remote file. "
					+ library.ssh_get_error(libSshSession.getPointer()));
			}
		} finally {
			library.sftp_close(file);
		}
	}

	public void put(String path, InputStream in, int permissions) throws SshException {
	}

	public void setLastModified(String path, long modtime) throws SshException {
	}

	private SftpFile attributesToFile(String path, sftp_attributes_struct attr) {
		String fullPath = attr.name == null ? path : Util.concatenatePaths(path, attr.name.getString(0));
		SftpFile file = new SftpFile(attr.type, fullPath, attr.size, attr.mtime * 1000l, attr.createtime * 1000l,
			attr.atime * 1000l, attr.gid, attr.uid, attr.permissions);
		library.sftp_attributes_free(attr);
		return file;
	}

	public void chmod(String path, int permissions) throws SshException {
		int ret = library.sftp_chmod(sftp, path, permissions);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to set permissions. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}

	public void chown(String path, int uid) throws SshException {
		sftp_attributes_struct attr = library.sftp_stat(sftp, path);
		int ret = library.sftp_chown(sftp, path, uid, attr.gid);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to set owner. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}

	}

	public void chgrp(String path, int gid) throws SshException {
		sftp_attributes_struct attr = library.sftp_stat(sftp, path);
		int ret = library.sftp_chown(sftp, path, attr.uid, gid);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException(SshException.GENERAL, "Failed to set group. "
				+ library.ssh_get_error(libSshSession.getPointer()));
		}
	}
}
