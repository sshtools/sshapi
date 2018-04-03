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
package net.sf.sshapi.vfs;

import java.io.IOException;
import java.util.Collection;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;

/**
 * FileSystem implementation for SFTP via SSHAPI
 */
public class SftpFileSystem extends AbstractFileSystem {

	final static Log log = LogFactory.getLog(SftpFileSystem.class);

	private SshClient ssh;
	private SftpClient sftp;

	private String home;

	protected SftpFileSystem(final GenericFileName rootName, final SshClient ssh, final FileSystemOptions fileSystemOptions) {
		super(rootName, null, fileSystemOptions);
		this.ssh = ssh;
	}

	protected void doCloseCommunicationLink() {
		try {
			if (sftp != null) {
				sftp.close();
				sftp = null;
			}

			if (ssh != null) {
				ssh.disconnect();
				ssh = null;
			}
		} catch (Exception ex) {
			// #ifdef DEBUG
			log.debug("Failed to close communication link.", ex);
			// #endif
		}
	}

	protected SftpClient getClient() throws IOException {
		if (this.ssh == null) {
			SshClient ssh;
			try {
				final GenericFileName rootName = (GenericFileName) getRootName();

				ssh = SshClientFactory.createConnection(rootName.getHostName(), rootName.getPort(), rootName.getUserName(),
					rootName.getPassword(), getFileSystemOptions());
			} catch (final Exception e) {
				throw new FileSystemException("vfs.provider.sftp/connect.error", getRootName(), e);
			}

			this.ssh = ssh;
		}

		try {
			/*
			 * We always maintain at least one sftp client all the while the
			 * connection is opened. If the client is in use (i.e. it hasn't be
			 * release by calling the putClient() method), then a new one is
			 * created.
			 */
			final SftpClient sftp;
			if (this.sftp != null) {
				sftp = this.sftp;
				this.sftp = null;
			} else {
				sftp = ssh.createSftpClient();
				sftp.open();
				home = sftp.getDefaultPath();
			}
			return sftp;
		} catch (final SshException e) {
			throw new FileSystemException("vfs.provider.sftp/connect.error", getRootName(), e);
		}
	}

	protected void putClient(final SftpClient sftp) {
		if (this.sftp == null) {
			this.sftp = sftp;
		} else {
			try {
				sftp.close();
			} catch (Exception e) {
			}
		}
	}

	protected void addCapabilities(final Collection caps) {
		caps.addAll(SftpFileProvider.capabilities);
	}

	protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
		return new SftpFileObject(name, this);
	}

	public double getLastModTimeAccuracy() {
		return 1L;
	}

	/**
	 * Get the default path (as it is when first connected, usually the users
	 * home).
	 * 
	 * @return home path
	 */
	public String getHome() {
		return home;
	}
}
