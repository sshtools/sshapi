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
package net.sf.sshapi.fuse.fs;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jnr.ffi.Pointer;
import jnr.ffi.Struct.Signed32;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpClient.OpenMode;
import net.sf.sshapi.sftp.SftpError;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

public class FuseSFTP extends FuseStubFS implements Closeable {
	private static Logger LOG = SshConfiguration.getLogger();
	
	class DirectoryHandle implements Closeable {
		DirectoryStream<SftpFile> stream;
		Iterator<SftpFile> iterator;
		public long offset;
		
		public DirectoryHandle(DirectoryStream<SftpFile> stream) {
			this.stream = stream;
			iterator = stream.iterator();
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}
	}

	private static final int MAX_READ_BUFFER_SIZE = 65536;
	private static final int MAX_WRITE_BUFFER_SIZE = 65536;
	private AtomicLong fileHandle = new AtomicLong();
	private Map<Long, DirectoryHandle> directoryHandles = new ConcurrentHashMap<>();
	private Map<Long, SftpHandle> handles = new ConcurrentHashMap<>();
	private Map<Long, Set<OpenMode>> flags = new ConcurrentHashMap<>();
	private Map<String, List<Long>> handlesByPath = new ConcurrentHashMap<>();
	private SftpClient sftp;
	private Path root;

	public FuseSFTP(SftpClient sftp) {
		this(sftp, sftp.getDefaultPath());
	}

	public FuseSFTP(SftpClient sftp, String root) {
		this(sftp, Paths.get(root));
	}

	public FuseSFTP(SftpClient sftp, Path path) {
		if(!sftp.getSshClient().getProvider().getCapabilities().contains(Capability.RAW_SFTP)) {
			throw new UnsupportedOperationException(String.format("Provider %s does not support %s.", sftp.getSshClient().getProvider().getName(), Capability.RAW_SFTP));
		}
		this.root = path;
		this.sftp = sftp;
	}
	
	@Override
	public synchronized int chmod(String path, @mode_t long mode) {
		path = translatePath(path);
		int ex = exists(path);
		if (ex != -ErrorCodes.EEXIST())
			return ex;
		try {
			sftp.chmod(path, (int) mode);
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to chmod {}0 to {1}",e,  path, mode);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to chmod {0} to {1}", e, path, mode);
			return -ErrorCodes.EFAULT();
		}
	}

	private String translatePath(String path) {
		while(path.startsWith("/"))
			path = path.substring(1);
		if(path.equals(""))
			return root.toString();
		else {
			path = root.resolve(path).toString();
			return path;
		}
	}

	@Override
	public synchronized int chown(String path, long uid, long gid) {
		path = translatePath(path);
		int ex = exists(path);
		if (ex != -ErrorCodes.EEXIST())
			return ex;
		try {
			sftp.chown(path, (int)uid);
			sftp.chgrp(path, (int)gid);
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to chown {0} to {1}:{2}", e, path, uid, gid);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to chmod {0} to {1}:{2}", e, path, uid, gid);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized int create(String path, @mode_t long mode, FuseFileInfo fi) {
		path = translatePath(path);
		int ex = exists(path);
		if (ex == -ErrorCodes.EEXIST())
			return ex;
		fi.flags.set(fi.flags.get() | 0x0100);
		return doOpen(path, fi);
	}

	@Override
	public synchronized int getattr(String path, FileStat stat) {
		path = translatePath(path);
		try {
			return fillStat(stat, sftp.stat(path), path);
		} catch (SftpException sftpse) {
			if (LOG.isDebug() && (LOG.isTrace() || sftpse.getCode() != SftpException.SSH_FX_NO_SUCH_FILE))
				LOG.debug("Error retrieving attributes for {0}", sftpse, path);
			return toErr(sftpse);
		} catch (Exception e) {
			LOG.error("Error retrieving attributes for {0}", e, path);
		}
		return -ErrorCodes.EREMOTEIO();
	}

	@Override
	public synchronized int mkdir(String path, @mode_t long mode) {
		path = translatePath(path);
		int ex = exists(path);
		if (ex != -ErrorCodes.ENOENT())
			return ex;
		try {
			sftp.mkdirs(path);
			
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to create directory {0}", e, path);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to create directory {0}", e, path);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized int open(String path, FuseFileInfo fi) {
		path = translatePath(path);
		return doOpen(path, fi);
	}

	private int doOpen(String path, FuseFileInfo fi) {
		try {
			long handle = fileHandle.getAndIncrement();
			Set<OpenMode> flgs = convertFlags(fi.flags);
			SftpHandle file;
			
			/* TODO: hrm, we seem to get a READ and TRUNCATE when opening for read!? BAD, This 
			 * will wipe out files */
			if(flgs.contains(OpenMode.SFTP_READ)) {
				flgs.remove(OpenMode.SFTP_TRUNC);
			}
			file = sftp.file(path, flgs.toArray(new OpenMode[0]));

			fi.fh.set(handle);

			handles.put(handle, file);
			flags.put(handle, flgs);
			List<Long> l = handlesByPath.get(path);
			if (l == null) {
				l = new ArrayList<Long>();
				handlesByPath.put(path, l);
			}
			l.add(handle);
			
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to open {0}", e, path);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to open {0}", e, path);
			return -ErrorCodes.EFAULT();
		}
	}

	@SuppressWarnings("resource")
	@Override
	public synchronized int truncate(String path, long size) {
		path = translatePath(path);
		try {
			/*
			 * This is a bit of a pain. truncate() may occur after an open(),
			 * but this is too late to send an O_TRUNC flag. So instead, we
			 * close the original handle, then re-open it truncated.
			 * 
			 * There could be multiple handles open, so we need to deal with all
			 * of them. The open flags for each are also remembered and used to
			 * re-open (minus the truncate flag for the 2nd handle up to the
			 * last).
			 * 
			 * We also don't get given FuseFileInfo, so need to maintain our own
			 * state of what files are open for a root.
			 * 
			 * If the file is not open then just truncate by opening a new file
			 * with O_TRUN.
			 */

			List<Long> pathHandles = handlesByPath.get(path);
			int idx = 0;
			for (Long l : pathHandles) {
				SftpHandle file = handles.get(l);
				file.close();
				Set<OpenMode> flgs = new LinkedHashSet<>(flags.get(l));
				if (idx == 0) {
					// For the first handle, re-open with truncate,
					flgs.add(OpenMode.SFTP_TRUNC);
					flgs.add(OpenMode.SFTP_CREAT);
					file = sftp.file(path, flgs.toArray(new OpenMode[0]));

					handles.put(l, file);
				} else {
					flgs.remove(OpenMode.SFTP_TRUNC);
					flgs.remove(OpenMode.SFTP_CREAT);
					file = sftp.file(path, flgs.toArray(new OpenMode[0]));
				}
				handles.put(l, file);
				idx++;
			}
			if (idx == 0) {
				// No open files
				SftpHandle file = sftp.file(path, OpenMode.SFTP_TRUNC, OpenMode.SFTP_CREAT);
				file.close();
			}
			
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to open {0}", e, path);
			return toErr(e);
		} catch (IOException e) {
			LOG.error("Failed to open {0}", e, path);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
		path = translatePath(path);
		try {
			SftpHandle file = handles.get(fi.fh.longValue());
			if (file == null)
				return -ErrorCodes.ESTALE();
			ByteBuffer b = ByteBuffer.allocate(Math.min(MAX_READ_BUFFER_SIZE, (int) size));
			int read;
			file.position(offset);
			read = file.read(b);
			if(read == -1) {
				return 0;
			}
			buf.put(0, b.array(), 0, read);
			return read;
		} catch (SftpException e) {
			LOG.error("Failed to open {0}", e, path);
			return toErr(e);
		} 
	}

	@Override
	public synchronized int readlink(String path, Pointer buf, long size) {
		path = translatePath(path);
		try {
			buf.putString(0, sftp.readLink(path), 0, Charset.defaultCharset());
			return 0;
		} catch (SftpException sftpse) {
			if (LOG.isDebug() && (LOG.isTrace() || sftpse.getCode() != SftpException.SSH_FX_NO_SUCH_FILE))
				LOG.debug("Error retrieving attributes for {0}.", sftpse, path);;
			return toErr(sftpse);
		} catch (Exception e) {
			LOG.error("Error retrieving attributes for {0}.", e, path);
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public synchronized int opendir(String path, FuseFileInfo fi) {
		path = translatePath(path);
		try {
			long handle = fileHandle.getAndIncrement();
			directoryHandles.put(handle, new DirectoryHandle(sftp.directory(path)));
			fi.fh.set(handle);
			return 0;
		} catch (SftpError e) {
			LOG.error("Failed to open dir {0}", e, path);
			return toErr(e.getSftpCause());
		} catch (SftpException e) {
			LOG.error("Failed to open dir {0}", e, path);
			return toErr(e);
		} 
	}

	@Override
	public synchronized int releasedir(String path, FuseFileInfo fi) {
		path = translatePath(path);
		return release(path, fi);
	}

	@Override
	public synchronized int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {

		path = translatePath(path);
		if (LOG.isInfo()) {
			LOG.info("Reading directory {0}", path);
		}
		try {
			DirectoryHandle file = directoryHandles.get(fi.fh.longValue());
			if (file == null) {
				LOG.warn("No handle {0} for {1}", fi.fh.longValue(), path);
				return -ErrorCodes.ESTALE();
			}
			if (offset != file.offset) {
				LOG.warn("Unexpected offset for {0}. Expected {1}, got {2}", path, file.offset, offset);
				return -ErrorCodes.ESTALE();
			}
			
			if(offset==0) {
				filter.apply(buf, ".", null, ++offset);
				file.offset = offset;
				if (!path.equals("/")) {
					filter.apply(buf, "..", null, ++offset);
					file.offset = offset;
				}
			}
			while(file.iterator.hasNext()) {
				SftpFile f = file.iterator.next();
				if(filter.apply(buf, f.getName(), null, ++offset) == 1) {
					/**
					 * According to https://www.cs.hmc.edu/~geoff/classes/hmc.cs135.201001/homework/fuse/fuse_doc.html#readdir-details
					 * we return zero when the buffer is full. We need to store the current offset for resumption
					 */
					file.offset = offset;
					return 0;
				}
			}
			file.offset = offset;
			
			return 0;
		} catch (SftpError e) {
			LOG.error("Failed to open {0}", e, path);
			return toErr(e.getSftpCause());
		} 
	}

	@Override
	public synchronized int release(String path, FuseFileInfo fi) {
		path = translatePath(path);

		SftpHandle file = handles.remove(fi.fh.longValue());
		if(file == null) {
			DirectoryHandle dir  = directoryHandles.remove(fi.fh.longValue());
			if(dir == null) {
				return -ErrorCodes.ESTALE();
			}
			else {
				try {
					dir.close();
				} catch (IOException e) {
					LOG.warn("Failed to close {}", path);
				}	
			}
		}
		else {
			List<Long> l = handlesByPath.get(path);
			flags.remove(fi.fh.longValue());
			if (l != null) {
				l.remove(fi.fh.longValue());
				if (l.isEmpty())
					handlesByPath.remove(path);
			}
			try {
				file.close();
			} catch (IOException e) {
				LOG.warn("Failed to close {0}", path);
			}	
		}
		return 0;
			
	}

	@Override
	public synchronized int rename(String oldpath, String newpath) {
		oldpath = translatePath(oldpath);
		newpath = translatePath(newpath);
		// TODO forgiveness / permission?
		int ex = exists(oldpath);
		if (ex != -ErrorCodes.EEXIST())
			return ex;
		try {
			sftp.rename(oldpath, newpath);
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to rename {0} to {1}", e, oldpath, newpath);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to rename {0} to {1}", e, oldpath, newpath);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized int rmdir(String path) {
		return unlink(path);
	}

	@Override
	public synchronized int symlink(String oldpath, String newpath) {
		oldpath = translatePath(oldpath);
		newpath = translatePath(newpath);
		int ex = exists(oldpath);
		if (ex != -ErrorCodes.EEXIST())
			return ex;
		try {
			sftp.symlink(oldpath, newpath);
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to symlink {0} to {1}", e, oldpath, newpath);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to remove {0} to {1}", e, oldpath, newpath);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized  int unlink(String path) {
		path = translatePath(path);
		int ex = exists(path);
		if (ex != -ErrorCodes.EEXIST())
			return ex;
		try {
			sftp.rm(path);
			return 0;
		} catch (SftpException e) {
			LOG.error("Failed to remove {0}", e, path);
			return toErr(e);
		} catch (SshException e) {
			LOG.error("Failed to remove {0}", e, path);
			return -ErrorCodes.EFAULT();
		}
	}

	@Override
	public synchronized int write(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
		path = translatePath(path);
		try {
			SftpHandle file = handles.get(fi.fh.longValue());
			if (file == null)
				return -ErrorCodes.ESTALE();
			ByteBuffer b = ByteBuffer.allocate(Math.min(MAX_WRITE_BUFFER_SIZE, (int) size));
			buf.get(0, b.array(), 0, b.capacity());
			file.write(b);
			return b.capacity();
		} catch (SftpException e) {
			LOG.error("Failed to open {0}", e, path);
			return toErr(e);
		} 
	}

	int exists(String path) {
		try {
			sftp.stat(path);
			return -ErrorCodes.EEXIST();
		} catch (SftpException sftpse) {
			if (sftpse.getCode() == SftpException.SSH_FX_NO_SUCH_FILE) {
				return -ErrorCodes.ENOENT();
			} else {
				return -ErrorCodes.EFAULT();
			}
		} catch (Exception e) {
			LOG.error("Error checking for existance for {}.", e, path);
			return -ErrorCodes.EFAULT();
		}
	}

	private Set<OpenMode> convertFlags(Signed32 flags) {
		Set<OpenMode> modes = new LinkedHashSet<>();
		int f = 0;
		int fv = flags.get();
		if ((fv & 0x0001) > 0 || (fv & 0x0002) > 0)
			modes.add(OpenMode.SFTP_WRITE);
		if ((fv & 0x0008) > 0)
			modes.add(OpenMode.SFTP_TEXT);
		if ((fv & 0x0100) > 0)
			modes.add(OpenMode.SFTP_CREAT);
		if ((fv & 0x0200) > 0)
			modes.add(OpenMode.SFTP_EXCL);
		if ((fv & 0x0800) > 0)
			modes.add(OpenMode.SFTP_TRUNC);
		if ((fv & 0x1000) > 0)
			modes.add(OpenMode.SFTP_APPEND);
		if (f == 0 || fv == 0 || (fv & 0x0002) > 0)
			modes.add(OpenMode.SFTP_READ);
		return modes;
	}

	private int fillStat(FileStat stat, SftpFile file, String path) throws SftpException {
		// TODO can probably do more linux for unix-to-unix file systems
		
		/**
		 * There are issues here. If you set the ownership to the uid on the remote server then
		 * the permissions may not apply to the user the file system is running for. Since
		 * we cannot rely on uid to be a valid uid on this system we have to use something
		 * consistent.
		 */
		stat.st_uid.set(0);
		stat.st_gid.set(0);
		 
		stat.st_atim.tv_sec.set(file.getAccessed() / 1000);
		stat.st_mtim.tv_sec.set(file.getLastModified() / 1000);
		stat.st_ctim.tv_sec.set(file.getCreated() / 1000);
		
		stat.st_size.set(file.getSize());
		
		/**
		 * Get the type.
		 * 
		 * NOTE: Some providers return this in the {@link SftpFile#getPermissions()}
		 * value, some do not. This should probably be made consistent.
		 */
		int mode = FileStat.S_IFREG;
		if(file.isDirectory())
			mode = FileStat.S_IFDIR;
		else if(file.isLink())
			mode = FileStat.S_IFLNK;
		
		mode = mode | file.getPermissions();
		
		/**
		 * This could probably be more intelligent and set "other" permissions,
		 * which is the permissions this user is likely to access them under given
		 * the note above about uid. Setting to all permissions ensures the user
		 *  on this device can access the file but the remote server may deny access.
		 */
		if(file.isDirectory()) {
			mode = mode | 0777;
		} else {
			mode = mode | 0666;
		}

		stat.st_mode.set(mode);

		return 0;
	}

	private int toErr(SftpException e) {
		if (e.getCode() == SftpException.SSH_FX_OK)
			return 0;
		else if (e.getCode() == SftpException.SSH_FX_NO_SUCH_FILE)
			return -ErrorCodes.ENOENT();
		else if (e.getCode() == SftpException.SSH_FX_NOT_A_DIRECTORY)
			return -ErrorCodes.ENOTDIR();
		else if (e.getCode() == SftpException.SSH_FX_PERMISSION_DENIED)
			return -ErrorCodes.EPERM();
		else if (e.getCode() == SftpException.SSH_FX_NO_CONNECTION)
			return -ErrorCodes.ENOTCONN();
		else if (e.getCode() == SftpException.SSH_FX_CONNECTION_LOST)
			return -ErrorCodes.ECONNRESET();
		else if (e.getCode() == SftpException.SSH_FX_OP_UNSUPPORTED)
			return -ErrorCodes.ENOSYS();
		else if (e.getCode() == SftpException.SSH_FX_FILE_ALREADY_EXISTS)
			return -ErrorCodes.EEXIST();
		else if (e.getCode() == SftpException.SSH_FX_BAD_MESSAGE)
			return -ErrorCodes.EBADMSG();
		else if (e.getCode() == SftpException.SSH_FX_DIR_NOT_EMPTY)
			return -ErrorCodes.ENOTEMPTY();
		else if (e.getCode() == SftpException.SSH_FX_FILE_IS_A_DIRECTORY)
			return -ErrorCodes.EISDIR();
		else if (e.getCode() == SftpException.SSH_FX_NO_SPACE_ON_FILESYSTEM)
			return -ErrorCodes.ENOSPC();
		else if (e.getCode() == SftpException.SSH_FX_QUOTA_EXCEEDED)
			return -ErrorCodes.EDQUOT();
		else if (e.getCode() == SftpException.SSH_FX_INVALID_PARAMETER)
			return -ErrorCodes.EINVAL();
		else
			return -ErrorCodes.EFAULT();
		// public static final int SSH_FX_EOF = 1;
		// /** No such file was found **/
		// public static final int SSH_FX_FAILURE = 4;
		// /** The client sent a bad protocol message **/
		// public static final int SSH_FX_INVALID_HANDLE = 9;
		// /** The root is invalid */
		// public static final int SSH_FX_NO_SUCH_PATH = 10;
		// /** Cannot write to remote location */
		// public static final int SSH_FX_WRITE_PROTECT = 12;
		// /** There is no media available at the remote location */
		// public static final int SSH_FX_NO_MEDIA = 13;
		//
		// // These error codes are not part of the supported versions however
		// are
		// // included as some servers are returning them.
		// public static final int SSH_FX_UNKNOWN_PRINCIPAL = 16;
		// public static final int SSH_FX_LOCK_CONFLICT = 17;
		// public static final int SSH_FX_INVALID_FILENAME = 20;
		// public static final int SSH_FX_LINK_LOOP = 21;
		// public static final int SSH_FX_CANNOT_DELETE = 22;
		// public static final int SSH_FX_BYTE_RANGE_LOCK_CONFLICT = 25;
		// public static final int SSH_FX_BYTE_RANGE_LOCK_REFUSED = 26;
		// public static final int SSH_FX_DELETE_PENDING = 27;
		// public static final int SSH_FX_FILE_CORRUPT = 28;
		// public static final int SSH_FX_OWNER_INVALID = 29;
		// public static final int SSH_FX_GROUP_INVALID = 30;
		// public static final int SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK = 31;
	}

	@Override
	public void close() throws IOException {
		umount();
	}
}