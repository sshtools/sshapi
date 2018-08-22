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
package net.sf.sshapi.impl.ganymed;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SFTPException;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3DirectoryEntry;
import ch.ethz.ssh2.SFTPv3FileAttributes;
import ch.ethz.ssh2.SFTPv3FileHandle;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.Util;

class GanymedSftpClient extends AbstractSftpClient {

	private final Connection session;
	private SFTPv3Client client;

	public GanymedSftpClient(Connection session) {
		this.session = session;
	}

	@Override
	public void onClose() throws SshException {
		client.close();
	}

	@Override
	public InputStream get(String path, final long filePointer) throws SshException, FileNotFoundException {
		final SftpFile file = stat(path);
		try {
			final SFTPv3FileHandle handle = client.openFileRO(path);
			try {
				final byte[] buf = new byte[32768];
				return new InputStream() {
					private long fileOffset = filePointer;
					private byte[] b = new byte[1];

					@Override
					public int read() throws IOException {
						int r = read(b, 0, 1);
						if (r == -1) {
							return -1;
						}
						return b[0];
					}

					@Override
					public int read(byte[] b, int off, int len) throws IOException {
						if (fileOffset >= file.getSize()) {
							return -1;
						}
						int r = client.read(handle, fileOffset, buf, 0, buf.length);
						if (r > 0) {
							fileOffset += r;
						}
						return r;
					}

					@Override
					public void close() throws IOException {
						client.closeFile(handle);
					}
				};
			} finally {
				client.closeFile(handle);
			}
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public InputStream get(String path) throws SshException, FileNotFoundException {
		return get(path, 0);
	}

	@Override
	public void get(String path, OutputStream out, long filePointer) throws SshException, FileNotFoundException {
		SftpFile file = stat(path);
		try {
			SFTPv3FileHandle handle = client.openFileRO(path);
			try {
				byte[] buf = new byte[32768];
				long fileOffset = filePointer;
				while (fileOffset < file.getSize()) {
					int r = client.read(handle, fileOffset, buf, 0, buf.length);
					out.write(buf, 0, r);
					fileOffset += r;
				}
				out.flush();
			} finally {
				client.closeFile(handle);
			}
		} catch (SFTPException sftpe) {
			if (translateError("Failed to open file", path, sftpe))
				throw new FileNotFoundException(String.format("Could not open file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void get(String path, OutputStream out) throws SshException, FileNotFoundException {
		get(path, out, 0l);
	}

	@Override
	public OutputStream put(String path, int permissions) throws SshException, FileSystemException {
		return super.put(path, permissions, 0);
	}

	@Override
	public OutputStream put(String path, int permissions, final long offset) throws SshException, FileSystemException {
		SftpFile currentFile = null;
		try {
			currentFile = stat(path);
		} catch (SshException sshe) {
			if (sshe.getCode() != SftpException.SSH_FX_NO_SUCH_FILE) {
				throw sshe;
			}
		}

		try {
			SFTPv3FileHandle handle;
			if (currentFile == null) {
				SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
				attr.permissions = Integer.valueOf(permissions);
				handle = client.createFileTruncate(path, attr);
			} else {
				handle = client.openFileRW(path);
			}
			final SFTPv3FileHandle finalHandle = handle;
			return new OutputStream() {

				@Override
				public void write(int b) throws IOException {
					try {
						client.write(finalHandle, offset, new byte[] { (byte) b }, 0, 1);
					} catch (IOException ioe) {
						throw ioe;
					} catch (Exception e) {
						IOException ioException = new IOException();
						ioException.initCause(e);
						throw ioException;
					}
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					try {
						client.write(finalHandle, offset, b, off, len);
					} catch (IOException ioe) {
						throw ioe;
					} catch (Exception e) {
						IOException ioException = new IOException();
						ioException.initCause(e);
						throw ioException;
					}
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void close() throws IOException {
					client.closeFile(finalHandle);
				}
			};
		} catch (SFTPException sftpe) {
			if (translateError("Failed to write file", path, sftpe))
				throw new FileSystemException(String.format("Could not write file. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void put(String path, InputStream in, int permissions) throws SshException, FileSystemException {
		SftpFile currentFile = null;
		try {
			currentFile = stat(path);
		} catch (SshException sshe) {
			if (sshe.getCode() != SftpException.SSH_FX_NO_SUCH_FILE) {
				throw sshe;
			}
		}

		try {
			SFTPv3FileHandle handle;
			if (currentFile == null) {
				SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
				attr.permissions = Integer.valueOf(permissions);
				handle = client.createFileTruncate(path, attr);
			} else {
				handle = client.openFileRW(path);
			}
			try {
				byte[] buf = new byte[32768];
				long fileOffset = 0;
				while (true) {
					int r = in.read(buf);
					if (r == -1) {
						break;
					}
					try {
						client.write(handle, fileOffset, buf, 0, r);
					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						throw new SshException(e);
					}
					fileOffset += r;
				}
			} finally {
				client.closeFile(handle);
			}
		} catch (SFTPException sftpe) {
			if (translateError("Failed to write file", path, sftpe))
				throw new FileSystemException(String.format("Could not write file. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}

	}

	@Override
	public void rename(String path, String newPath) throws SshException, FileSystemException {
		try {
			client.mv(path, newPath);
		} catch (SFTPException sftpe) {
			if (translateError("Failed to rename file", path, sftpe))
				throw new FileSystemException(String.format("Could not rename file. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}

	}

	@Override
	public SftpFile[] ls(String path) throws SshException, FileNotFoundException {
		try {
			List<SftpFile> files = new ArrayList<>();
			for (SFTPv3DirectoryEntry e : client.ls(path))
				files.add(entryToFile(path, e));
			return files.toArray(new SftpFile[files.size()]);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to list directory", path, sftpE))
				throw new FileNotFoundException(String.format("Could not list directory. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException("Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3DirectoryEntry entry) {
		String fullPath = Util.concatenatePaths(path, entry.filename);
		SFTPv3FileAttributes attributes = entry.attributes;
		SftpFile file = new SftpFile(convertType(attributes), fullPath,
				attributes.size == null ? 0 : attributes.size.longValue(), convertIntDate(attributes.mtime), 0,
				convertIntDate(attributes.atime), toInt(attributes.gid), toInt(attributes.uid),
				toInt(attributes.permissions));
		return file;
	}

	int toInt(Integer i) {
		return i == null ? 0 : i.intValue();
	}

	long convertIntDate(Integer date) {
		return date == null ? 0 : date.intValue() * 1000l;
	}

	@Override
	public void onOpen() throws SshException {
		try {
			client = new SFTPv3Client(session);
		} catch (IOException e) {
			throw new SshException("Failed to start SFTP client.", e);
		}
	}

	@Override
	public String getDefaultPath() throws SshException {
		// TODO return the home dir?
		return "/";
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException, FileSystemException {
		try {
			client.mkdir(path, permissions);
		} catch (SFTPException sftpe) {
			if (translateError("Failed to create directory", path, sftpe))
				throw new FileSystemException(String.format("Could not create directory. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rm(String path) throws SshException, FileSystemException {
		try {
			client.rm(path);
		} catch (SFTPException sftpe) {
			if (translateError("Failed to remove file", path, sftpe))
				throw new FileSystemException(String.format("Could not remove file. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rmdir(String path) throws SshException, FileSystemException {
		try {
			client.rmdir(path);
		} catch (SFTPException sftpe) {
			if (translateError("Failed to remove directory", path, sftpe))
				throw new FileSystemException(String.format("Could not remove directory. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpe);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SftpFile stat(String path) throws SshException, FileNotFoundException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			return entryToFile(path, entry);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to remove directory", path, sftpE))
				throw new FileNotFoundException(String.format("Could not find file. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException, FileSystemException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.mtime = Integer.valueOf((int) (modtime / 1000));
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to set last modified time", path, sftpE))
				throw new FileSystemException(String.format("Could not set last modified time. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3FileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.size == null ? 0 : entry.size.longValue(),
				convertIntDate(entry.mtime), 0, convertIntDate(entry.atime), toInt(entry.gid), toInt(entry.uid),
				toInt(entry.permissions));
	}

	int convertType(SFTPv3FileAttributes attrs) {
		if (attrs.isDirectory()) {
			return SftpFile.TYPE_DIRECTORY;
		} else if (attrs.isSymlink()) {
			return SftpFile.TYPE_LINK;
		} else if (attrs.isRegularFile()) {
			return SftpFile.TYPE_FILE;
		} else {
			return SftpFile.TYPE_UNKNOWN;
		}
	}

	/*
	 * The SftpClient.write method is re-implemented here as it is bugged in the
	 * current release of Ganymed. Again, reflection is needed to work around this
	 */

	// private void XXXXwrite(SFTPv3FileHandle handle, long fileOffset, byte[] src,
	// int srcoff, int len)
	// throws IOException, SecurityException, NoSuchMethodException,
	// NoSuchFieldException,
	// IllegalArgumentException, IllegalAccessException, InvocationTargetException,
	// SftpException {
	// if (handle.getClient() != client)
	// throw new IOException("The file handle was created with another
	// SFTPv3FileHandle instance.");
	//
	// if (handle.isClosed())
	// throw new IOException("The file handle is closed.");
	//
	// // if (len < 0) wtf?
	// while (len > 0) {
	// int writeRequestLen = len;
	//
	// if (writeRequestLen > 32768)
	// writeRequestLen = 32768;
	//
	// Method m = client.getClass().getDeclaredMethod("generateNextRequestID", new
	// Class[0]);
	// m.setAccessible(true);
	// int req_id = ((Integer) m.invoke(client, null)).intValue();
	//
	// TypesWriter tw = new TypesWriter();
	// Field f = handle.getClass().getDeclaredField("fileHandle");
	// f.setAccessible(true);
	// byte[] fileHandle = (byte[]) f.get(handle);
	//
	// tw.writeString(fileHandle, 0, fileHandle.length);
	// tw.writeUINT64(fileOffset);
	// tw.writeString(src, srcoff, writeRequestLen);
	//
	// m = client.getClass().getDeclaredMethod("sendMessage", new Class[] {
	// int.class, int.class, byte[].class });
	// m.setAccessible(true);
	// m.invoke(client,
	// new Object[] { Integer.valueOf(Packet.SSH_FXP_WRITE),
	// Integer.valueOf(req_id), tw.getBytes() });
	//
	// fileOffset += writeRequestLen;
	//
	// srcoff += writeRequestLen;
	// len -= writeRequestLen;
	//
	// m = client.getClass().getDeclaredMethod("receiveMessage", new Class[] {
	// int.class });
	// m.setAccessible(true);
	// byte[] resp = (byte[]) m.invoke(client, new Object[] { Integer.valueOf(34000)
	// });
	//
	// TypesReader tr = new TypesReader(resp);
	//
	// int t = tr.readByte();
	//
	// int rep_id = tr.readUINT32();
	// if (rep_id != req_id)
	// throw new IOException("The server sent an invalid id field.");
	//
	// if (t != Packet.SSH_FXP_STATUS)
	// throw new IOException("The SFTP server sent an unexpected packet type (" + t
	// + ")");
	//
	// int errorCode = tr.readUINT32();
	//
	// if (errorCode == ErrorCodes.SSH_FX_OK)
	// continue;
	//
	// String errorMessage = tr.readString();
	// throw new SftpException(errorCode, errorMessage);
	// }
	// }

	@Override
	public void chmod(String path, int permissions) throws SshException, FileSystemException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.permissions = new Integer(permissions);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to set file permissions", path, sftpE))
				throw new FileSystemException(String.format("Could not set file permissions. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file permissions.", e);
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException, FileSystemException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.uid = new Integer(uid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to set file owner", path, sftpE))
				throw new FileSystemException(String.format("Could not set file owner. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file owner.", e);
		}
	}

	@Override
	public void chgrp(String path, int gid) throws SshException, FileSystemException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.gid = new Integer(gid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			if (translateError("Failed to set file group", path, sftpE))
				throw new FileSystemException(String.format("Could not set file group. %s", path));
			else
				throw new SshException(SshException.GENERAL, sftpE);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file group.", e);
		}
	}

	private boolean translateError(String message, String path, SFTPException ex) throws SshException {
		int code = ex.getServerErrorCode();
		if (code == 0x01)
			return true;
		throw new SshException(SshException.GENERAL, String.format("%s. Error %d. %s [%s]", message, code,
				ex.getServerErrorCodeSymbol(), ex.getServerErrorCodeVerbose()));
	}
}
