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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferClient;
import net.sf.sshapi.SshLifecycleListener;

/**
 * Providers will create an instance of an implementation o	f this interface to
 * access the SFTP system. All paths provides should be fully qualified. Some
 * providers internally may provide 'pwd' and 'cd' like functionality, but as
 * not all do SSHAPI does NOT expose this.
 * 
 * @see SshClient#sftp()
 */
public interface SftpClient extends SshFileTransferClient<SshLifecycleListener<SftpClient>, SftpClient> {
	/**
	 * Whether or not to transfer files according to end-of-line settings when
	 * using {@link SftpClient#get(String)} and
	 * {@link SftpClient#put(String, int)} methods. The provider must support
	 * {@link Capability#SFTP_TRANSFER_MODE} and the server must also support
	 * it.
	 *
	 */
	public enum TransferMode {
		
		/** Files transferred using {@link SftpClient#get(String)} and {@link SftpClient#put(String, int)} methods will be transformed using the configured end-of-line settings. */
		TEXT,
		/**
		 * Files transferred using {@link SftpClient#get(String)} and
		 * {@link SftpClient#put(String, int)} methods will be not be
		 * transformed at all.
		 */
		BINARY
	}

	/**
	 * How to transform end-of-line sequenceswhen {@link TransferMode#TEXT} is
	 * in use. Note this is only a hint, not all providers have to support all
	 * of the policies. The provider must support
	 * {@link Capability#SFTP_TRANSFER_MODE} and the server must also support
	 * it.
	 */
	public enum EOLPolicy {
		/**
		 * Whether to force the remote end of line.
		 */
		FORCE_REMOTE,
		
		/** Use LF on the remote end. */
		REMOTE_LF,
		
		/** Use CR on the remote end. */
		REMOTE_CR,
		
		/** Use CR,LF on the remote end. */
		REMOTE_CR_LF,
		
		/** Use LF on the local end. */
		LOCAL_LF,
		
		/** Use CR on the local end. */
		LOCAL_CR,
		
		/** Use CR_LF on the local end. */
		LOCAL_CR_LF;
		
		/**
		 * To string.
		 *
		 * @return the string
		 */
		@Override
		public String toString() {
			switch (this) {
			case REMOTE_LF:
			case LOCAL_LF:
				return "\n";
			case REMOTE_CR:
			case LOCAL_CR:
				return "\r";
			case REMOTE_CR_LF:
			case LOCAL_CR_LF:
				return "\r\n";
			default:
				return "";
			}
		}
	}

	/**
	 * Mode supplied to {@link SftpClient#file(String, OpenMode...)}.
	 */
	public enum OpenMode {
		
		/** sftp read. */
		SFTP_READ,
		/** sftp write. */
		SFTP_WRITE,
		/** sftp append. */
		SFTP_APPEND,
		/** sftp creat. */
		SFTP_CREAT,
		/** sftp trunc. */
		SFTP_TRUNC,
		/** sftp excl. */
		SFTP_EXCL,
		/** sftp text. */
		SFTP_TEXT;
		
		/**
		 * To POSIX.
		 *
		 * @return the int
		 */
		public int toPOSIX() {
			switch (this) {
			case SFTP_READ:
				return 00;
			case SFTP_WRITE:
				return 01;
			case SFTP_TEXT:
				return 010;
			case SFTP_APPEND:
				return 02000;
			case SFTP_CREAT:
				return 0100;
			case SFTP_TRUNC:
				return 01000;
			case SFTP_EXCL:
				return 0200;
			default:
				return 0;
			}
		}
		
		/**
		 * To int.
		 *
		 * @return the int
		 */
		public int toInt() {
			switch (this) {
			case SFTP_READ:
				return 1;
			case SFTP_WRITE:
				return 2;
			case SFTP_APPEND:
				return 4;
			case SFTP_CREAT:
				return 8;
			case SFTP_TRUNC:
				return 16;
			case SFTP_EXCL:
				return 32;
			case SFTP_TEXT:
				return 64;
			default:
				return 0;
			}
		}

		/**
		 * To POSIX.
		 *
		 * @param modes the modes
		 * @return the int
		 */
		public static int toPOSIX(OpenMode... modes) {
			int m = 0;
			for (OpenMode mode : modes) {
				m = m | mode.toPOSIX();
			}
			return m;
		}

		/**
		 * To flags.
		 *
		 * @param modes the modes
		 * @return the int
		 */
		public static int toFlags(OpenMode... modes) {
			int m = 0;
			for (OpenMode mode : modes) {
				m = m | mode.toInt();
			}
			return m;
		}
	}

	/**
	 * Set whether or not to transform files according to end-of-line settings,
	 * or transfer them with no transformation.
	 * 
	 * @param transferMode mode
	 * @return this for chaining
	 */
	SftpClient mode(TransferMode transferMode);

	/**
	 * Get whether or not to transform files according to end-of-line settings,
	 * or transfer them with no transformation.
	 * 
	 * @return transferMode mode
	 */
	TransferMode mode();

	/**
	 * Set the hint as to how to transform files according to end-of-line
	 * settings when {@link TransferMode#TEXT} is set.
	 * 
	 * @param eolPolicies end of line policies
	 * @return this for chaining
	 * @see {@link TransferMode}
	 * @see {@link #eol()}
	 */
	SftpClient eol(EOLPolicy... eolPolicies);
	
	/**
	 * Convenience method to test if a file exists (underneath will use stat()).
	 * 
	 * @param path path of remote file
	 * @return exists
	 * @throws SshException on error
	 */
	boolean exists(String path) throws SshException;

	/**
	 * Get the hint as to how to transform files according to end-of-line
	 * settings when {@link TransferMode#TEXT} is set.
	 * 
	 * @return eol end of line transformation policies
	 * @see {@link TransferMode}
	 * @see {@link #eol(EOLPolicy)}
	 */
	EOLPolicy[] eol();

	/**
	 * Open a file for reading. The provider must support
	 * {@link Capability#RAW_SFTP}, or {@link UnsupportedOperationException}
	 * will be thrown.
	 * 
	 * @param path path of file to open
	 * @param modes modes
	 * @return handle
	 * @throws SshException on error
	 * @throws UnsupportedOperationException if not supported
	 */
	SftpHandle file(String path, OpenMode... modes) throws SshException;


	/**
	 * Open a directory for reading as a stream. The provider may override the
	 * default implementation (which uses {@link #ls(String)}), for a more efficient
	 * algorithm that doesn't load all entries into memory before returning them.
	 * Remember to {@link DirectoryStream#close()} the stream when done. The
	 * unchecked {@link SftpError} will be thrown if errors occur during iteration
	 * or creation of iterators.
	 * 
	 * @param path path of directory to open
	 * @return closable stream
	 * @throws SshException on error
	 * @throws UnsupportedOperationException if not supported
	 * @see #directory(String, java.nio.file.DirectoryStream.Filter)
	 */
	default DirectoryStream<SftpFile> directory(String path) throws SftpException {
		return directory(path, f -> true);
	}

	/**
	 * Open a directory for reading as a stream. The provider may override the
	 * default implementation (which uses {@link #ls(String)}), for a more efficient
	 * algorithm that doesn't load all entries into memory before returning them.
	 * Remember to {@link DirectoryStream#close()} the stream when done. The
	 * unchecked {@link SftpError} will be thrown if errors occur during iteration
	 * or creation of iterators.
	 * 
	 * @param path   path of directory to open
	 * @param filter filter
	 * @return closable stream
	 * @throws SshException                  on error
	 * @throws UnsupportedOperationException if not supported
	 * @see #directory(String)
	 * 
	 */
	default DirectoryStream<SftpFile> directory(String path, DirectoryStream.Filter<SftpFile> filter) throws SftpException {
		return new DirectoryStream<SftpFile>() {
			
			boolean closed = false;
			
			@Override
			public void close() throws IOException {
				closed = true;
			}

			@Override
			public Iterator<SftpFile> iterator() {
				if(closed)
					throw new SftpError("Closed.");
				try {
					return Arrays.asList(ls(path)).stream().filter(f -> {
						try {
							return filter == null || filter.accept(f);
						} catch (IOException e) {
							throw new SftpError("Failed to filter.", e);
						}
					}).collect(Collectors.toList()).iterator();
				} catch (SftpException e) {
					throw new IllegalStateException(e);
				} catch (SshException e) {
					throw new IllegalStateException(e);
				}
			}
			
		};
	}

	/**
	 * List a directory. Note, this method does not match wildcards (either for the path to list of the
	 * files contained within). 
	 * 
	 * @param path directory to list
	 * @return files contained in directory
	 * @throws SshException on other error
	 */
	SftpFile[] ls(String path) throws SshException;

	/**
	 * Iterate over a directory. This is preferable method to use if you are likely
	 * to be iterating over unusually large amounts of files, and the provider supports
	 * iterable listing.  
	 * 
	 * @param path directory to list
	 * @return iterable of files contained in directory
	 * @throws SshException on other error
	 */
	Iterable<SftpFile> list(String path) throws SshException;

	/**
	 * Visit all the files starting at a path. This can recursively (or not) scan the 
	 * entire file tree and perform operations on those files as it goes. Vistors should
	 * throw the unchecked {@link SftpError} if errors occur visitor handling. 
	 * 
	 * @param path directory to visit
	 * @param visitor visitor
	 * @return result
	 * @throws SshException on other error
	 */
	FileVisitResult visit(String path, FileVisitor<SftpFile> visitor) throws SshException;

	/**
	 * Get the default path.
	 *
	 * @return default path
	 */
	String getDefaultPath();

	/**
	 * Get an SFTP file object (containing all of it's attributes) given a path
	 * in the remote file system. This method follows links.
	 * 
	 * @param path path
	 * @return file object
	 * @throws SshException on other error
	 */
	SftpFile stat(String path) throws SshException;

	/**
	 * Get an SFTP file object (containing all of it's attributes) given a path
	 * in the remote file system. This method does NOT follow links.
	 * 
	 * @param path path
	 * @return file object
	 * @throws SshException on other error
	 */
	SftpFile lstat(String path) throws SshException;

	/**
	 * Get a the actual path given the path to a link. T@Override
	he provider must support
	 * {@link Capability#SFTP_READ_LINK} or an {@link UnsupportedOperationException}
	 * will be thrown. The target returned will be an absolute path name.
	 * 
	 * @param path path
	 * @return full qualified target
	 * @throws SshException on other error
	 */
	String readLink(String path) throws SshException;

	/**
	 * Create a directory. If any element of the parent path does not exist an
	 * exception will be thrown.
	 * 
	 * @param path path of directory to create.
	 * @throws SshException on other error
	 */
	void mkdir(String path) throws SshException;

	/**
	 * Create a directory. If any element of the parent path does not exist an
	 * exception will be thrown.
	 * 
	 * @param path path of directory to create.
	 * @param permissions permissions
	 * @throws SshException on other error
	 */
	void mkdir(String path, int permissions) throws SshException;

	/**
	 * Create a directory, creating missing parents. If any element of the
	 * parent path does not exist it will be created.
	 *
	 * @param dir the dir
	 * @throws SshException on other error
	 */
	void mkdirs(String dir) throws SshException;

	/**
	 * Create a directory, creating missing parents. If any element of the
	 * parent path does not exist it will be created.
	 *
	 * @param path path of directory to create.
	 * @param permissions permissions
	 * @throws SshException on other error
	 */
	void mkdirs(String path, int permissions) throws SshException;

	/**
	 * Remove a file given it's path. If the path is a directory, use
	 * {@link #rmdir(String)} instead.
	 * 
	 * @param path path of file to remove.
	 * @throws SshException on other error
	 */
	void rm(String path) throws SshException;

	/**
	 * Remove a file or directory, optionally recursively.
	 *
	 * @param path path of file or directory to remove.
	 * @param recursive the recursive
	 * @throws SshException on other error
	 */
	void rm(String path, boolean recursive) throws SshException;

	/**
	 * Create a symbolic link. The target is relative to the symlink itself. For 
	 * example, if the path was <strong>myfolder/file.link</strong>, then for the
	 * actual file to be in the same folder, but with a different name, the target would
	 * be <strong>myoriginalfile</strong>. If it were in the parent directory, then it
	 * would be <strong>../myoriginalfile</strong>. If it were in some directory not easily
	 * reachable with a relative path, it would be <strong>/home/me/myoriginalfile</strong>.
	 * 
	 * @param path path of file to symlink.
	 * @param target path to point symlink to
	 * @throws SshException on other error
	 * 
	 */
	void symlink(String path, String target) throws SshException;

	/**
	 * Create a hard link. The target is relative to the link itself. For 
	 * example, if the path was <strong>myfolder/file.link</strong>, then for the
	 * actual file to be in the same folder, but with a different name, the target would
	 * be <strong>myoriginalfile</strong>. If it were in the parent directory, then it
	 * would be <strong>../myoriginalfile</strong>. If it were in some directory not easily
	 * reachable with a relative path, it would be <strong>/home/me/myoriginalfile</strong>.
	 * 
	 * @param path path of file to link.
	 * @param target path to point link to
	 * @throws SshException on other error
	 * 
	 */
	void link(String path, String target) throws SshException;
	
	/**
	 * Remove a directory given it's path.
	 * 
	 * @param path path of directory to remove.
	 * @throws SshException on other error
	 */
	void rmdir(String path) throws SshException;

	/**
	 * Rename or move a file. If a file or folder on the remove system is on a
	 * different file system, a move may not be possible.
	 * 
	 * @param path path of file or directory to rename or move.
	 * @param newPath new path of file or directory
	 * @throws SshException on other error
	 */
	void rename(String path, String newPath) throws SshException;

	/**
	 * Change the permission of a remove file.
	 * 
	 * @param path path of remote file
	 * @param permissions permissions
	 * @throws SshException on other error
	 */
	void chmod(String path, int permissions) throws SshException;

	/**
	 * Change the owner of a remote file.
	 * 
	 * @param path path of remote file
	 * @param uid new UID
	 * @throws SshException on other error
	 */
	void chown(String path, int uid) throws SshException;

	/**
	 * Change the owning group of a remote file.
	 * 
	 * @param path path of remote file
	 * @param gid new GID
	 * @throws SshException on other error
	 */
	void chgrp(String path, int gid) throws SshException;

	/**
	 * Set the last modified time of a file.
	 *
	 * @param path the path
	 * @param modtime last modified time in milliseconds since 00:00:00, Jan 1st
	 *            1970.
	 * @throws SshException on other error
	 */
	void setLastModified(String path, long modtime) throws SshException;

	/**
	 * Get the version of the SFTP protocol in use. If this cannot be
	 * determined, zero will be returned.
	 * 
	 * @return sftp protocol version or zero
	 */
	int getSftpVersion();
	
	/**
	 * Get the parent {@link SshClient} that owns this SFTP client.
	 * 
	 *  @return parent client
	 */
	SshClient getSshClient();

	/**
	 * Retrieve the contents of a remote file, presenting it as an input stream.
	 * This method will return immediately for the input stream to be read.
	 * Remember to close the input stream when you are done with it.
	 * 
	 * @param path path of remote file
	 * @return input stream
	 * @throws SshException on other error
	 * @see {@link #get(String, OutputStream)}
	 */
	InputStream get(String path) throws SshException;

	/**
	 * Retrieve the contents of a remote file, writing it to the local file. If
	 * destination is a directory, the file will be retrieved to that directory
	 * (overwriting any previous file of the same name). If it is an existing
	 * file, it will be overwritten. If it doesn't exist at all, it will be
	 * created. If the remote file is a directory, an exception will be thrown.
	 * 
	 * @param path path of remote file
	 * @param destination destination file or directory
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void get(String path, File destination) throws SshException, IOException;

	/**
	 * Resume the download of a remote file, writing it to the local file. If
	 * destination is a directory, the file will be retrieved to that directory
	 * (overwriting any previous file of the same name). If it is an existing
	 * file, the download will resume, appending to it. If it doesn't exist at
	 * all, it will be created and download. If the remote file is a directory,
	 * an exception will be thrown.
	 * 
	 * @param path path of remote file
	 * @param destination destination file or directory
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void resumeGet(String path, File destination) throws SshException, IOException;

	/**
	 * Resume the upload of a local file, writing it to the remote file. 
	 * 
	 * @param source source file or directory
	 * @param path path of remote file
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void resumePut(File source, String path) throws SshException, IOException;

	/**
	 * Retrieve the contents of a remote file, presenting it as an input stream.
	 * This method will return immediately for the input stream to be read.
	 * Remember to close the input stream when you are done with it. A file
	 * pointer should be provided indicating the position in the file to start
	 * the stream. This may be used to 'resume' downloads.
	 * <p>
	 * If the provider doesn't support setting the file pointer, an exception
	 * will be thrown.
	 *
	 * @param path path of remote file
	 * @param filePointer position to set filePointer to
	 * @return input stream
	 * @throws SshException on other error
	 * @throws UnsupportedOperationException uoe
	 * @see {@link #get(String, OutputStream)}
	 */
	InputStream get(String path, long filePointer) throws SshException;

	/**
	 * Retrieve the contents of a remote file, writing it to the provided output
	 * stream. This method will block until all data has been received.
	 * 
	 * @param path path of remote file
	 * @param out output stream to write data to
	 * @throws SshException on other error
	 * @see {@link #get(String)}
	 */
	void get(String path, OutputStream out) throws SshException;

	/**
	 * Retrieve the contents of a remote file, writing it to the provided output
	 * stream. This method will block until all data has been received. A file
	 * pointer should be provided indicating the position in the file to start
	 * the stream. This may be used to 'resume' downloads.
	 * <p>
	 * If the provider doesn't support setting the file pointer, an exception
	 * will be thrown.
	 * 
	 * @param path path of remote file
	 * @param out output stream to write data to
	 * @param filePointer position to set filePointer to
	 * @throws SshException on other error
	 * @see {@link #get(String)}
	 */
	void get(String path, OutputStream out, long filePointer) throws SshException;

	/**
	 * Upload the contents of a local file or directory, writing it to the
	 * remote file or directory. Any existing file or directory will be
	 * overwritten
	 * 
	 * @param source file or directory
	 * @param path path of remote file
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void put(File source, String path) throws SshException, IOException;

	/**
	 * Upload the contents of a local file or directory, writing it to the
	 * remote file or directory. Any existing file or directory will be
	 * overwritten
	 * 
	 * @param source file or directory
	 * @param path path of remote file
	 * @param permissions directory permission
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void put(File source, String path, int permissions) throws SshException, IOException;

	/**
	 * Write to a remote from the provided input stream. This method will block
	 * until the input stream reports EOF. Note, it is up to the caller to close
	 * the input stream when complete.
	 * 
	 * @param path path to save file to
	 * @param in input stream providing content
	 * @throws SshException on other error
	 */
	void put(String path, InputStream in) throws SshException;

	/**
	 * Write to a remote from the provided input stream. This method will block
	 * until the input stream reports EOF. Note, it is up to the caller to close
	 * the input stream when complete.
	 * 
	 * @param path path to save file to
	 * @param in input stream providing content
	 * @param permissions permissions of file when created
	 * @throws SshException on other error
	 */
	void put(String path, InputStream in, int permissions) throws SshException;

	/**
	 * Write to a remote from the provided input stream. This method will block
	 * until the input stream reports EOF. Note, it is up to the caller to close
	 * the input stream when complete.
	 * 
	 * @param path path to save file to
	 * @param in input stream providing content
	 * @param permissions permissions of file when created
	 * @param offset offset in remote file to write to
	 * @throws SshException on other error
	 */
	void put(String path, InputStream in, int permissions, long offset) throws SshException;

	/**
	 * Open a remote file for writing to. This method will return immediately,
	 * with any bytes written to the returned output stream written to the
	 * remote file.
	 * 
	 * @param path path to save file to
	 * @return output stream
	 * @throws SshException on other error
	 */
	OutputStream put(String path) throws SshException;

	/**
	 * Open a remote file for writing to. This method will return immediately,
	 * with any bytes written to the returned output stream written to the
	 * remote file.
	 * 
	 * @param path path to save file to
	 * @param permissions permissions of file when created
	 * @return output stream
	 * @throws SshException on other error
	 */
	OutputStream put(String path, int permissions) throws SshException;

	/**
	 * Open a remote file for writing to. This method will return immediately,
	 * with any bytes written to the returned output stream written to the
	 * remote file. A file pointer should be provided indicating the position in
	 * the file to start writing. This may be used to 'resume' uploads.
	 *
	 * @param path path to save file to
	 * @param permissions permissions of file when created
	 * @param offset the offset
	 * @return output stream
	 * @throws SshException on other error
	 */
	OutputStream put(final String path, final int permissions, long offset) throws SshException;

	/**
	 * Get the detected remote eol policy. The provider must support
	 * {@link Capability} and the server must support this too.
	 * 
	 * @return end of line policy
	 */
	EOLPolicy getRemoteEOL();

	/**
	 * Copy the contents of a local directory into a remote directory.
	 * 
	 * @param localdir the path to the local directory
	 * @param remotedir the remote directory which will receive the contents
	 * @param recurse recurse through child folders
	 * @param sync synchronize the directories by removing files on the remote
	 *            server that do not exist locally
	 * @param commit actually perform the operation. If <tt>false</tt> a
	 *            <a href="DirectoryOperation.html">DirectoryOperation</a> will
	 *            be returned so that the operation can be evaluated and no
	 *            actual files will be created/transfered.
	 * 
	 * @return SftpOperation information about the copy
	 * 
	 * @throws SshException on any error
	 */
	SftpOperation upload(File localdir, String remotedir, boolean recurse, boolean sync, boolean commit) throws SshException;

	/**
	 * Copy the contents of a remote directory to a local directory.
	 *
	 * @param remotedir the remote directory whose contents will be copied.
	 * @param localdir the local directory to where the contents will be copied
	 * @param recurse recurse into child folders
	 * @param sync synchronized the directories by removing files and
	 *            directories that do not exist on the remote server.
	 * @param commit actually perform the operation. If <tt>false</tt> the
	 *            operation will be processed and a
	 *            <a href="DirectoryOperation.html">DirectoryOperation</a> will
	 *            be returned without actually transfering any files.
	 * @return SftpOperation information about the copy
	 * @throws SshException on any error
	 */
	SftpOperation download(String remotedir, File localdir, boolean recurse, boolean sync, boolean commit) throws SshException;
}
