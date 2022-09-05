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
package net.sf.sshapi.vfs;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.RandomAccessMode;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpClient.OpenMode;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.Util;

/**
 * {@link AbstractFileObject} for SFTP via SSHAPI.
 */
public class SftpFileObject extends AbstractFileObject<SftpFileSystem> {

	private final SftpFileSystem fs;
	private SftpFile attrs;
	private String relPath;

	protected SftpFileObject(final AbstractFileName name,
			final SftpFileSystem fs) throws FileSystemException {
		super(name, fs);
		this.fs = fs;
        relPath = UriParser.decode(fs.getRootName().getRelativeName(name));
	}

	protected FileType doGetType() throws Exception {

		if (attrs == null) {
			return FileType.IMAGINARY;
		}

		if (attrs.isDirectory()) {
			return FileType.FOLDER;
		}
		return FileType.FILE;
	}

	private void statSelf() throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			attrs = sftp.stat(getSftpPath());
		} catch (final SshException e) {
			// Does not exist
			attrs = null;
		} finally {
			fs.putClient(sftp);
		}
	}

	protected void doCreateFolder() throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			sftp.mkdir(getSftpPath(), -1);
			statSelf();
		} finally {
			fs.putClient(sftp);
		}
	}

	protected long doGetLastModifiedTime() throws Exception {
		if (attrs == null) {
			throw new FileSystemException(
					"vfs.provider.sftp/get-last-modified-time.error");
		}
		return attrs.getLastModified();
	}

	protected boolean doSetLastModifiedTime(final long modtime)
			throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			sftp.setLastModified(getSftpPath(), modtime);
			statSelf();
			return true;
		} finally {
			fs.putClient(sftp);
		}
	}

	protected void doDelete() throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			if (FileType.FOLDER.equals(getType())) {
				sftp.rmdir(getSftpPath());
			} else {
				sftp.rm(getSftpPath());
			}
		} finally {
			fs.putClient(sftp);
		}
	}

	protected void doRename(FileObject newfile) throws Exception {
		final SftpClient sftp = fs.getClient();
		try {

			String oldName = getSftpPath();
			String newName = newfile.getName().getPathDecoded();
			if (oldName.equals(newName)) {
				throw new FileSystemException(
						"vfs.provider.sftp/rename-identical-files",
						new Object[] { newName });
			}
			sftp.rename(oldName, newName);
		} finally {
			fs.putClient(sftp);
		}
	}

	protected String[] doListChildren() throws Exception {
		// List the contents of the folder
		final SftpFile[] array;
		final SftpClient sftp = fs.getClient();
		try {
			array = sftp.ls(getSftpPath());
		} finally {
			fs.putClient(sftp);
		}
		if (array == null) {
			throw new FileSystemException(
					"vfs.provider.sftp/list-children.error");
		}

		// Extract the child names
		final ArrayList<String> children = new ArrayList<>();
		for (int i = 0; i < array.length; i++) {
			if (!array[i].getName().equals(".")
					&& !array[i].getName().equals("..")) {
				children.add(array[i].getName());
			}
		}
		return UriParser.encode((String[]) children.toArray(new String[children
				.size()]));
	}

	protected void doSetAttribute(String attrName, Object value) throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			if(attrName.equals("gid")) {
				sftp.chgrp(getSftpPath(), Integer.parseInt(value.toString()));
			}
			else if(attrName.equals("uid")) {
				sftp.chown(getSftpPath(), Integer.parseInt(value.toString()));
			}
			else if(attrName.equals("permissions")) {
				sftp.chmod(getSftpPath(), Integer.parseInt(value.toString()));
			}
			else if(attrName.equals("maskString")) {
				
				sftp.chmod(getSftpPath(), Integer.parseInt(value.toString()));
			}
			else {
				throw new FileSystemException(
						"vfs.provider.sftp/set-attribute.error");
			}
		} catch (final SshException e) {
		} finally {
			fs.putClient(sftp);
		}
	}

	protected Map<String, Object> doGetAttributes() throws Exception {
		final Map<String, Object> attributes = new HashMap<>();
		if (attrs != null) {
			attributes.put("accessedTime", Long.valueOf(attrs.getAccessed()));
			attributes.put("creationTime", Long.valueOf(attrs.getCreated()));
			attributes.put("gid", Integer.valueOf(attrs.getGID()));
			attributes.put("maskString",
					Util.getMaskString(attrs.getPermissions()));
			attributes.put("permissions",
					Integer.valueOf(attrs.getPermissions()));
			attributes.put(
					"permissionsString",
					Util.getPermissionsString(attrs.getType(),
							attrs.getPermissions()));
			attributes.put("uid", Integer.valueOf(attrs.getUID()));
			attributes.put("block", attrs.isBlock());
			attributes.put("character", attrs.isCharacter());
			attributes.put("fifo", attrs.isFIFO());
			attributes.put("link", attrs.isLink());
			attributes.put("socket", attrs.isSocket());
			attributes.put("directory", attrs.isDirectory());
			attributes.put("file", attrs.isFile());
			attributes.put("type", attrs.getType().name());
		}
		return attributes;
	}

	protected long doGetContentSize() throws Exception {
		if (attrs == null) {
			statSelf();
			if (attrs == null) {
				throw new FileSystemException(
						"vfs.provider.sftp/get-content-size.error");
			}
		}
		return attrs.getSize();
	}

	protected RandomAccessContent doGetRandomAccessContent(
			final RandomAccessMode mode) throws Exception {
		if(fs.getClient().getSshClient().getProvider().getCapabilities().contains(Capability.RAW_SFTP)) {
			final SftpClient sftp = fs.getClient();
			try {
				String path = getSftpPath();
				final SftpHandle handle = mode == RandomAccessMode.READWRITE ? sftp.file(path, OpenMode.SFTP_READ, OpenMode.SFTP_WRITE) :   sftp.file(path, OpenMode.SFTP_READ);
				return new SftpRawContent(path, handle, mode, sftp, fs);
			}
			catch(Exception e) {
				fs.putClient(sftp);
				throw e;
			}
		}
		else
			return new SftpRandomAccessContent(this, mode);
	}

	/**
	 * Creates an input stream to read the file content from.
	 */
	InputStream getInputStream(long filePointer) throws IOException {

		final SftpClient sftp = fs.getClient();
		try {
			final InputStream get = sftp.get(getSftpPath(), filePointer);
			return new FilterInputStream(get) {
				public void close() throws IOException {
					try {
						super.close();
					} finally {
						fs.putClient(sftp);
					}
				}
			};
		} catch (Exception e) {
			fs.putClient(sftp);
			if(e instanceof IOException) {
				throw (IOException)e;
			}
			else {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		}
	}

	protected InputStream doGetInputStream() throws Exception {
		final SftpClient sftp = fs.getClient();
		try {
			final InputStream get = sftp.get(getSftpPath());
			return new FilterInputStream(get) {
				public void close() throws IOException {
					try {
						super.close();
					} finally {
						fs.putClient(sftp);
					}
				}
			};
		} catch (Exception e) {
			fs.putClient(sftp);
			throw e;
		}
		
	}

	protected void doAttach() throws Exception {
		super.doAttach();
		statSelf();
	}

	protected void doDetach() throws Exception {
		super.doDetach();
		attrs = null;
	}

	protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
		final SftpClient sftp = fs.getClient();
		long pointer = 0;
		if (bAppend) {
			statSelf();
			pointer = attrs.getSize();
		}
		try {
			final OutputStream put = sftp.put(getSftpPath(), -1,
					pointer);

			return new FilterOutputStream(put) {

				public void write(byte[] b, int off, int len)
						throws IOException {
					put.write(b, off, len);
				}

				public void close() throws IOException {
					try {
						super.close();
					} finally {
						fs.putClient(sftp);
					}

				}

			};
		} catch (Exception e) {
			fs.putClient(sftp);
			throw e;
		}

	}

	protected boolean doIsHidden() throws Exception {
		return getName().getBaseName().startsWith(".");
	}

	private String getSftpPath() throws FileSystemException {
		/* TODO: Temporary DEBUG */
		return getName().getPathDecoded();
	}
}
