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
package net.sf.sshapi.sftp;

import java.text.DateFormat;
import java.util.Date;

import net.sf.sshapi.util.Util;

/**
 * Represents a single file on the remote server.
 */
public class SftpFile {

	/**
	 * Type mnemonics (match those returned by 'ls' command)
	 */
	public static char[] TYPES = { ' ', '-', 'd', 'l', 'p', '?', 's', 'c', 'b' };

	/**
	 * File is a of <i>File</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_FILE = 1;

	/**
	 * File is a of <i>Directory</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_DIRECTORY = 2;

	/**
	 * File is a of <i>Symbolic Link</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_LINK = 3;

	/**
	 * File is a of <i>FIFO</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_FIFO = 4;

	/**
	 * File is a of an unknown type.
	 * 
	 * @see #getType
	 */
	public final static int TYPE_UNKNOWN = 5;

	/**
	 * File is a of <i>socket</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_SOCKET = 6;

	/**
	 * File is a of <i>character special device</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_CHARACTER = 7;

	/**
	 * File is a of <i>block special device</i> type
	 * 
	 * @see #getType
	 */
	public final static int TYPE_BLOCK = 8;

	private final String path;
	private final long size;
	private final long lastModified;
	private final String name;
	private final int type;
	private final int gid;
	private final int uid;
	private final long created;
	private final long accessed;
	private final int permissions;

	/** Permissions flag: Permits the owner of a file to read the file. */
	public final static int S_IRUSR = 0x100;

	/** Permissions flag: Permits the owner of a file to write to the file. */
	public final static int S_IWUSR = 0x80;

	/**
	 * Permissions flag: Permits the owner of a file to execute the file or to
	 * search the file's directory.
	 */
	public final static int S_IXUSR = 0x40;

	/** Permissions flag: Permits a file's group to read the file. */
	public final static int S_IRGRP = 0x20;

	/** Permissions flag: Permits a file's group to write to the file. */
	public final static int S_IWGRP = 0x10;

	/** Permissions flag: Permits others to read the file. */
	public final static int S_IROTH = 0x04;

	/** Permissions flag: Permits others to write to the file. */
	public final static int S_IWOTH = 0x02;

	/**
	 * Permissions flag: Permits a file's group to execute the file or to search
	 * the file's directory.
	 */
	public final static int S_IXGRP = 0x08;

	/**
	 * Permissions flag: Permits others to execute the file or to search the
	 * file's directory.
	 */
	public final static int S_IXOTH = 0x01;

	/**
	 * Constructor.
	 * 
	 * @param type see {@link #getType()}
	 * @param path full path to the file
	 * @param size size of file
	 * @param lastModified time in milliseconds since epoch file was last
	 *            modified
	 * @param created time in milliseconds the file was created
	 * @param accessed time in milliseconds the file was last accessed
	 * @param gid the groud ID
	 * @param uid the user ID
	 * @param permissions permissions
	 */
	public SftpFile(int type, String path, long size, long lastModified, long created, long accessed, int gid, int uid,
			int permissions) {
		this.path = path;
		this.size = size;
		this.gid = gid;
		this.uid = uid;
		this.lastModified = lastModified;
		this.created = created;
		this.permissions = permissions;
		this.accessed = accessed;
		this.type = type;

		name = Util.basename(path);
	}

	/**
	 * Convenience method to determine if the file is a directory.
	 * 
	 * @return directory
	 */
	public boolean isDirectory() {
		return type == TYPE_DIRECTORY;
	}

	/**
	 * Convenience method to determine if the file is a regular file.
	 * 
	 * @return regular file
	 */
	public boolean isFile() {
		return type == TYPE_FILE;
	}

	/**
	 * Convenience method to determine if the file is a symbolic link.
	 * 
	 * @return regular file
	 */
	public boolean isLink() {
		return type == TYPE_LINK;
	}

	/**
	 * Get the type of file. May be one of {@link #TYPE_FILE},
	 * {@link #TYPE_DIRECTORY}, {@link #TYPE_LINK}, {@link #TYPE_UNKNOWN},
	 * {@link #TYPE_FIFO}, {@link #TYPE_CHARACTER}, {@link #TYPE_BLOCK} or
	 * {@link #TYPE_LINK}.
	 * 
	 * 
	 * @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Get the absolute path of this file.
	 * 
	 * @return absolute path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Get the base name of this file (i.e. with out any path)
	 * 
	 * @return base name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the size of this file in bytes.
	 * 
	 * @return size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Get the time this file was last modified in milliseconds since the epoch,
	 * or when it was created if it has not yet. been modified.
	 * 
	 * @return last modified
	 */
	public long getLastModified() {
		return lastModified;
	}

	public String toString() {
		DateFormat sdf = DateFormat.getDateTimeInstance();
		return name + "\t" + getSize() + "\t" + sdf.format(new Date(getLastModified()));
	}

	/**
	 * Get the group ID (GID).
	 * 
	 * @return GID
	 */
	public int getGID() {
		return gid;
	}

	/**
	 * Get the user ID (UID)
	 * 
	 * @return UID
	 */
	public int getUID() {
		return uid;
	}

	/**
	 * Get the time in milliseconds the file was last accessed.
	 * 
	 * @return time in milliseconds the file was last accessed.
	 */
	public long getAccessed() {
		return accessed;
	}

	/**
	 * Get the time in milliseconds the file was created.
	 * 
	 * @return time in milliseconds the file was created.
	 */
	public long getCreated() {
		return created;
	}

	/**
	 * Get the permissions value.
	 * 
	 * @return permissions
	 */
	public int getPermissions() {
		return permissions;
	}
}
