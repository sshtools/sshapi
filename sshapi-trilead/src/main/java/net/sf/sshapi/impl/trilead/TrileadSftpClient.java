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
package net.sf.sshapi.impl.trilead;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPException;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.AbstractSftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.Util;

class TrileadSftpClient extends AbstractSftpClient<TrileadSshClient> {
	private final Connection session;
	private SFTPv3Client client;

	public TrileadSftpClient(TrileadSshClient client) {
		super(client);
		this.session = client.getNativeClient();
	}

	@Override
	public void onClose() throws SshException {
		client.close();
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		try {
			client.mv(path, newPath);
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		try {
			List<?> entries = client.ls(path);
			List<SftpFile> files = new ArrayList<>();
			for (Iterator<?> it = entries.iterator(); it.hasNext();) {
				files.add(entryToFile(path, (SFTPv3DirectoryEntry) it.next()));
			}
			return files.toArray(new SftpFile[files.size()]);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException("Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3DirectoryEntry entry) {
		String fullPath = Util.concatenatePaths(path, entry.filename);
		SFTPv3FileAttributes attributes = entry.attributes;
		SftpFile file = new SftpFile(convertType(attributes), fullPath, attributes.size == null ? 0 : attributes.size.longValue(),
				convertToMs(attributes.mtime), 0, convertToMs(attributes.atime), toInt(attributes.gid), toInt(attributes.uid),
				toInt(attributes.permissions));
		return file;
	}

	int toInt(Integer i) {
		return i == null ? 0 : i.intValue();
	}

	long convertToMs(Long date) {
		return date == null ? 0 : date.longValue() * 1000l;
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
	public SftpHandle file(String path, OpenMode... modes) throws SftpException {
		try {
			//  SFTPv3FileHandle openFile(String fileName, int flags, SFTPv3FileAttributes attr) throws IOException
			Method m = SFTPv3Client.class.getDeclaredMethod("openFile", String.class, int.class, SFTPv3FileAttributes.class);
			m.setAccessible(true);
			return createHandle((SFTPv3FileHandle) m.invoke(client, path, OpenMode.toFlags(modes), null));
		} catch (Exception ioe) {
			throw new SftpException(SftpException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	private SftpHandle createHandle(SFTPv3FileHandle nativeHandle) {
		return new SftpHandle() {
			private long position;
			private byte[] writeBuffer;
			private byte[] readBuffer;

			@Override
			public void close() throws IOException {
				try {
					client.closeFile(nativeHandle);
				} finally {
					writeBuffer = null;
					readBuffer = null;
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
					client.write(nativeHandle, position, writeBuffer, 0, len);
				} catch (IOException e) {
					throw new SftpException(SftpException.IO_ERROR, e);
				}
				position += len;
				return this;
			}

			@Override
			public int read(ByteBuffer buffer) throws SftpException {
				int len = buffer.limit() - buffer.position();
				if(len < 1)
					throw new SftpException(SftpException.OUT_OF_BUFFER_SPACE, "Run out of buffer space reading a file.");
				if (readBuffer == null || readBuffer.length != len) {
					readBuffer = new byte[len];
				}
				try {
					int read = client.read(nativeHandle, position, readBuffer, 0, len);
					if (read != -1) {
						buffer.put(readBuffer, 0, read);
						position += read;
					}
					return read;
				} catch (IOException e) {
					throw new SftpException(SftpException.IO_ERROR, e);
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
		};
	}

	@Override
	public String getDefaultPath() {
		// TODO return the home dir?
		return "/";
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			client.mkdir(path, permissions);
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public int getSftpVersion() {
		return client.getProtocolVersion();
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			String linkpath = Util.getAbsolutePath(path, getDefaultPath());
			String targetpath = Util.getAbsolutePath(target, Util.dirname(linkpath));
			switch(configuration.getSftpSymlinks()) {
			case SshConfiguration.STANDARD_SFTP_SYMLINKS:
				client.createSymlink(targetpath, linkpath);
				break;
			case SshConfiguration.OPENSSH_SFTP_SYMLINKS:
				/* Is default Ganymed behaviour */
				client.createSymlink(linkpath, targetpath);
				break;
			default:
				if(isOpenSSH()) {
					client.createSymlink(linkpath, targetpath);
				}
				else {
					client.createSymlink(targetpath, linkpath);
				}
				break;
			}
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rm(String path) throws SshException {
		try {
			client.rm(path);
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		try {
			client.rmdir(path);
		} catch (SFTPException sftpe) {
			throw new SftpException(sftpe.getServerErrorCode(), sftpe.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SftpFile lstat(String path) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.lstat(path);
			return entryToFile(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	protected String doReadLink(String path) throws SshException {
		try {
			return Util.linkPath(client.readLink(path), path);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public SftpFile stat(String path) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			return entryToFile(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.mtime = Long.valueOf((int) (modtime / 1000));
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3FileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.size == null ? 0 : entry.size.longValue(), convertToMs(entry.mtime), 0,
				convertToMs(entry.atime), toInt(entry.gid), toInt(entry.uid), toInt(entry.permissions));
	}

	int convertType(SFTPv3FileAttributes attrs) {
		/* BUG: For some reason I am seeing files getting reported as regular files AND links if they
		 * are regular files! So isRegularFile() needs to come before isSymlink() for best behaviour. 
		 */
		if (attrs.isDirectory()) {
			return SftpFile.TYPE_DIRECTORY;
		} else if (attrs.isRegularFile()) {
			return SftpFile.TYPE_FILE;
		} else if (attrs.isSymlink()) {
			return SftpFile.TYPE_LINK;
		}  else {
			return SftpFile.TYPE_UNKNOWN;
		}
	}
	/*
	 * The SftpClient.write method is re-implemented here as it is bugged in the
	 * current release of Ganymed. Again, reflection is needed to work around
	 * this
	 */

	// private void XXXXwrite(SFTPv3FileHandle handle, long fileOffset,
	// byte[] src, int srcoff, int len) throws IOException,
	// SecurityException, NoSuchMethodException, NoSuchFieldException,
	// IllegalArgumentException, IllegalAccessException,
	// InvocationTargetException, SftpException {
	// if (handle.getClient() != client)
	// throw new IOException(
	// "The file handle was created with another SFTPv3FileHandle instance.");
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
	// Method m = client.getClass().getDeclaredMethod(
	// "generateNextRequestID", new Class[0]);
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
	// m = client.getClass().getDeclaredMethod("sendMessage",
	// new Class[] { int.class, int.class, byte[].class });
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
	// m = client.getClass().getDeclaredMethod("receiveMessage",
	// new Class[] { int.class });
	// m.setAccessible(true);
	// byte[] resp = (byte[]) m.invoke(client,
	// new Object[] { Integer.valueOf(34000) });
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
	// throw new IOException(
	// "The SFTP server sent an unexpected packet type (" + t
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
	public void chmod(String path, int permissions) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.permissions = Integer.valueOf(permissions);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.uid = Integer.valueOf(uid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.gid = Integer.valueOf(gid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new SftpException(sftpE.getServerErrorCode(), sftpE.getLocalizedMessage());
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}
}
