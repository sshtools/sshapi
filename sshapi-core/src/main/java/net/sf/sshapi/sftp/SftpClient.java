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
package net.sf.sshapi.sftp;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshFileTransferClient;
import net.sf.sshapi.SshLifecycleListener;

/**
 * Providers will create an instance of an implementation of this interface to
 * access the SFTP system. All paths provides should be fully qualified. Some
 * providers internally may provide 'pwd' and 'cd' like functionality, but as not
 * all do SSHAPI does NOT expose this.
 * 
 * @see SshClient#createSftpClient()
 */
public interface SftpClient extends SshFileTransferClient<SshLifecycleListener<SftpClient>, SftpClient> {
	/**
	 * List a directory.
	 * 
	 * @param path directory to list
	 * @return files contain in directory
	 * @throws SshException on other error
	 */
	SftpFile[] ls(String path) throws SshException;

	/**
	 * Get the default path
	 * 
	 * @return default path
	 * @throws SshException
	 */
	String getDefaultPath() throws SshException;

	/**
	 * Get an SFTP file object (containing all of it's attributes) given a path
	 * in the remote file system.
	 * 
	 * @param path path
	 * @return file object
	 * @throws SshException on other error
	 */
	SftpFile stat(String path) throws SshException;

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
	 * @param path path of directory to create.
	 * @param permissions permissions
	 * @throws SshException on other error
	 * @throws FileNotFoundException 
	 */
	void mkdirs(String path, int permissions) throws SshException;

	/**
	 * Remove a file given it's path. If the path is a directory, use
	 * {@link #rmdir(String)} instead.
	 * 
	 * @param path path of file to remove.
	 * @throws SshException on other error
	 * 
	 */
	void rm(String path) throws SshException;

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
	 * Retrieve the contents of a remote file, writing it to the local file.
	 * If destination is a directory, the file will be retrieved to that
	 * directory (overwriting any previous file of the same name). If it is
	 * an existing file, it will be overwritten. If it doesn't exist at 
	 * all, it will be created.
	 * 
	 * @param path path of remote file
	 * @param destination destination file or directory
	 * @throws SshException on other error
	 * @throws IOException on error writing local file
	 * @see {@link #get(String)}
	 */
	void get(String path, File destination) throws SshException, IOException;

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
	 * @see {@link #get(String, OutputStream)}
	 * @throws UnsupportedOperationException uoe
	 */
	InputStream get(String path, long filePointer) throws SshException;

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
	 * Open a remote file for writing to. This method will return immediately,
	 * with any bytes written to the returned output stream written to the remote file.  A file
	 * pointer should be provided indicating the position in the file to start
	 * writing. This may be used to 'resume' uploads.
	 * 
	 * @param path path to save file to
	 * @param permissions permissions of file when created
	 * @param filePointer position to set filePointer to
	 * @return output stream
	 * @throws SshException on other error
	 */
	OutputStream put(final String path, final int permissions, long offset) throws SshException;

	/**
	 * Open a remote file for writing to. This method will return immediately,
	 * with any bytes written to the returned output stream written to the remote file. 
	 * 
	 * @param path path to save file to
	 * @param permissions permissions of file when created
	 * @return output stream
	 * @throws SshException on other error
	 */
	OutputStream put(String path, int permissions) throws SshException;

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
	 * @param path
	 * @param modtime last modified time in milliseconds since 00:00:00, Jan 1st 1970.
	 * @throws SshException on other error
	 */
	void setLastModified(String path, long modtime) throws SshException;

}
