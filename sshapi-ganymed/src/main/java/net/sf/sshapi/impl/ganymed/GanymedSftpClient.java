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
package net.sf.sshapi.impl.ganymed;

import java.io.IOException;
import java.nio.ByteBuffer;
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
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.Util;

class GanymedSftpClient extends AbstractSftpClient {
	private final Connection session;
	private SFTPv3Client client;
	private String home;

	public GanymedSftpClient(GanymedSshClient client, Connection session) {
		super(client.getProvider(), client.getConfiguration());
		this.session = session;
	}

	@Override
	public void onClose() throws SshException {
		client.close();
	}

	@Override
	public SftpHandle file(String path, OpenMode... modes) throws SftpException {
		try {
			return createHandle(client.openFile(path, OpenMode.toFlags(modes), null));
		} catch (IOException ioe) {
			throw new SftpException(SftpException.IO_ERROR, String.format("Failed to open file."), ioe);
		}
	}

	private SftpHandle createHandle(SFTPv3FileHandle nativeHandle) {
		return new SftpHandle() {
			private long position;
			private byte[] writeBuffer;
			private byte[] readBuffer;
			private boolean closed;
			private boolean errored;

			@Override
			public void close() throws IOException {
				try {
					if(!closed) {
						try {
							client.closeFile(nativeHandle);
						}
						catch(IOException ioe) {
							if(!errored)
								throw ioe;
						}
					}
				} finally {
					closed = true;
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
					errored = true;
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
					if(read != -1) { 
						buffer.put(readBuffer, 0, read);
						position += read;
					}
					else 
						closed = true;
					return read;
				} catch (IOException e) {
					errored = true;
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
	public int getSftpVersion() {
		return client.getProtocolVersion();
	}

	@Override
	public void symlink(String path, String target) throws SshException {
		try {
			client.createSymlink(path, target);
		} catch (SFTPException sftpe) {
			throw new GanymedSftpException(sftpe, String.format("Could not link file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rename(String path, String newPath) throws SshException {
		try {
			client.mv(path, newPath);
		} catch (SFTPException sftpe) {
			throw new GanymedSftpException(sftpe, String.format("Could not rename file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public SftpFile[] ls(String path) throws SshException {
		try {
			List<SftpFile> files = new ArrayList<>();
			for (SFTPv3DirectoryEntry e : client.ls(path))
				files.add(entryToFile(path, e));
			return files.toArray(new SftpFile[files.size()]);
		} catch (SFTPException sftpE) {
			throw new GanymedSftpException(sftpE, String.format("Could not list directory. %s", path));
		} catch (IOException e) {
			throw new SshException("Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3DirectoryEntry entry) {
		String fullPath = Util.concatenatePaths(path, entry.filename);
		SFTPv3FileAttributes attributes = entry.attributes;
		SftpFile file = new SftpFile(convertType(attributes), fullPath, attributes.size == null ? 0 : attributes.size.longValue(),
				convertIntDate(attributes.mtime), 0, convertIntDate(attributes.atime), toInt(attributes.gid), toInt(attributes.uid),
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
			home = client.canonicalPath("");
		} catch (IOException e) {
			throw new SshException("Failed to start SFTP client.", e);
		}
	}

	@Override
	public String getDefaultPath() {
		return home;
	}

	@Override
	public void mkdir(String path, int permissions) throws SshException {
		try {
			client.mkdir(path, permissions);
		} catch (SFTPException sftpe) {
			throw new GanymedSftpException(sftpe, String.format("Could not create directory. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rm(String path) throws SshException {
		try {
			client.rm(path);
		} catch (SFTPException sftpe) {
			throw new GanymedSftpException(sftpe, String.format("Could not remove file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void rmdir(String path) throws SshException {
		try {
			client.rmdir(path);
		} catch (SFTPException sftpe) {
			throw new GanymedSftpException(sftpe, String.format("Could not remove directory. %s", path));
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
			throw new GanymedSftpException(sftpE, String.format("Could not find file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	protected String doReadLink(String path) throws SshException {
		try {
			return Util.linkPath(client.readLink(path), path);
		} catch (SFTPException sftpE) {
			throw new GanymedSftpException(sftpE, String.format("Could not find file. %s", path));
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
			throw new GanymedSftpException(sftpE, String.format("Could not find file. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	@Override
	public void setLastModified(String path, long modtime) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.mtime = Integer.valueOf((int) (modtime / 1000));
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new GanymedSftpException(sftpE, String.format("Could not set last modified time. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to list directory.", e);
		}
	}

	private SftpFile entryToFile(String path, SFTPv3FileAttributes entry) {
		return new SftpFile(convertType(entry), path, entry.size == null ? 0 : entry.size.longValue(), convertIntDate(entry.mtime),
				0, convertIntDate(entry.atime), toInt(entry.gid), toInt(entry.uid), toInt(entry.permissions));
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
	 * current release of Ganymed. Again, reflection is needed to work around
	 * this
	 */

	// private void XXXXwrite(SFTPv3FileHandle handle, long fileOffset, byte[]
	// src,
	// int srcoff, int len)
	// throws IOException, SecurityException, NoSuchMethodException,
	// NoSuchFieldException,
	// IllegalArgumentException, IllegalAccessException,
	// InvocationTargetException,
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
	// Method m = client.getClass().getDeclaredMethod("generateNextRequestID",
	// new
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
	// byte[] resp = (byte[]) m.invoke(client, new Object[] {
	// Integer.valueOf(34000)
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
	// throw new IOException("The SFTP server sent an unexpected packet type ("
	// + t
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
			throw new GanymedSftpException(sftpE, String.format("Could not set file permissions. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file permissions.", e);
		}
	}

	@Override
	public void chown(String path, int uid) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.uid = Integer.valueOf(uid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new GanymedSftpException(sftpE, String.format("Could not set file owner. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file owner.", e);
		}
	}

	@Override
	public void chgrp(String path, int gid) throws SshException {
		try {
			SFTPv3FileAttributes entry = client.stat(path);
			entry.gid = Integer.valueOf(gid);
			client.setstat(path, entry);
		} catch (SFTPException sftpE) {
			throw new GanymedSftpException(sftpE, String.format("Could not set file group. %s", path));
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, "Failed to set file group.", e);
		}
	}

	@SuppressWarnings("serial")
	class GanymedSftpException extends SftpException {
		GanymedSftpException(SFTPException ex, String message) {
			super(ex.getServerErrorCode(), String.format("%s. Error %d. %s [%s]", message, ex.getServerErrorCode(),
					ex.getServerErrorCodeSymbol(), ex.getServerErrorCodeVerbose()), ex);
		}
	}
}
