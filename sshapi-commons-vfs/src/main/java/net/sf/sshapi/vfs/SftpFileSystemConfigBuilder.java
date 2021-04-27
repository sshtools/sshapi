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

import java.io.File;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.sftp.SftpClient;

/**
 * @{link {@link FileSystemConfigBuilder} for SFTP via SSHAPI.
 * 
 */
public class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder {
	private final static SftpFileSystemConfigBuilder builder = new SftpFileSystemConfigBuilder();

	/**
	 * Get a static instance.
	 * 
	 * @return instance
	 */
	public static SftpFileSystemConfigBuilder getInstance() {
		return builder;
	}

	private SftpFileSystemConfigBuilder() {
	}

	/**
	 * Set the file to use to store known hosts.
	 * 
	 * @param opts options
	 * @param sshdir directory contain known hosts file
	 */
	public void setKnownHosts(FileSystemOptions opts, File sshdir) {
		setParam(opts, "knownHosts", sshdir);
	}

	/**
	 * Get the file to use to store known hosts.
	 * 
	 * @param opts options
	 * @return directory contain known hosts file
	 */
	public File getKnownHosts(FileSystemOptions opts) {
		return (File) getParam(opts, "knownHosts");
	}

	/**
	 * Set the host key validator to use.
	 * 
	 * @param opts options
	 * @param hostKeyVerification host key validator
	 */
	public void setHostKeyVerification(FileSystemOptions opts, SshHostKeyValidator hostKeyVerification) {
		setParam(opts, "hostKeyVerification", hostKeyVerification);
	}

	/**
	 * Get the host key validator to use.
	 * 
	 * @param opts options
	 * @return host key validator
	 */
	public SshHostKeyValidator getHostKeyVerification(FileSystemOptions opts) {
		return (SshHostKeyValidator) getParam(opts, "hostKeyVerification");
	}

	/**
	 * Set the shared SSH client to use. If set, no additional connection will
	 * be made, instead a new {@link SftpClient} will be created fron the
	 * existing connection.
	 * 
	 * @param opts options
	 * @param sshClient client
	 */
	public void setSshClient(FileSystemOptions opts, SshClient sshClient) {
		setParam(opts, "sshClient", sshClient);
	}

	/**
	 * Get the shared SSH client to use. If set, no additional connection will
	 * be made, instead a new {@link SftpClient} will be created fron the
	 * existing connection.
	 * 
	 * @param opts options
	 * @return shared client
	 */
	public SshClient getSshClient(FileSystemOptions opts) {
		return (SshClient) getParam(opts, "sshClient");
	}

	/**
	 * Set the character encoding to use.
	 * 
	 * @param opts options
	 * @param charset character encoding
	 */
	public void setCharset(FileSystemOptions opts, String charset) {
		setParam(opts, "charset", charset);
	}

	/**
	 * Get the character encoding to use.
	 * 
	 * @param opts options
	 * @return character encoding
	 */
	public String getCharset(FileSystemOptions opts) {
		return (String) getParam(opts, "charset");
	}

	/**
	 * Set the files contain private keys (for authentication) to use.
	 * 
	 * @param opts options
	 * @param identities files contain private keys (for authentication) to use.
	 */
	public void setIdentities(FileSystemOptions opts, File[] identities) {
		setParam(opts, "identities", identities);
	}

	/**
	 * Get the files contain private keys (for authentication) to use.
	 * 
	 * @param opts options
	 * @return files contain private keys (for authentication) to use.
	 */
	public File[] getIdentities(FileSystemOptions opts) {
		return (File[]) getParam(opts, "identities");
	}

	/**
	 * Set the compression algorithm to use.
	 * 
	 * @param opts options
	 * @param compression compression
	 */
	public void setCompression(FileSystemOptions opts, String compression) {
		setParam(opts, "compression", compression);
	}

	/**
	 * Get the compression algorithm to use.
	 * 
	 * @param opts options
	 * @return compression
	 */
	public String getCompression(FileSystemOptions opts) {
		return (String) getParam(opts, "compression");
	}

	protected Class<? extends FileSystem> getConfigClass() {
		return SftpFileSystem.class;
	}
}
