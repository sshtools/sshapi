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
package net.sf.sshapi.impl.libssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sun.jna.Memory;

import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.Logger;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.util.Util;
import ssh.SshLibrary;
import ssh.SshLibrary.SizeT;
import ssh.SshLibrary.ssh_scp;
import ssh.SshLibrary.ssh_session;

/**
 * libssh SCP implementation.
 *
 */
public class LibsshSCPClient extends AbstractSCPClient implements SshSCPClient {
	final static Logger LOG = SshConfiguration.getLogger();
	private SshLibrary library;
	private ssh_session libSshSession;

	/**
	 * Constructor.
	 * 
	 * @param provider provider
	 * @param library library
	 * @param libSshSession session
	 */
	public LibsshSCPClient(SshProvider provider, SshLibrary library, ssh_session libSshSession) {
		super(provider);
		this.library = library;
		this.libSshSession = libSshSession;
	}

	@Override
	protected void onClose() throws net.sf.sshapi.SshException {
		// Nothing to do, we open and close the actual sessions when
		// transferring files
	}

	@Override
	protected void onOpen() throws net.sf.sshapi.SshException {
		// Nothing to do, we open and close the actual sessions when
		// transferring files
	}

	@Override
	public void get(String remoteFilePath, File destinationFile, boolean recursive) throws net.sf.sshapi.SshException {
		checkOpen();
		ssh_scp scp = library.ssh_scp_new(libSshSession, SshLibrary.SSH_SCP_READ | (recursive ? SshLibrary.SSH_SCP_RECURSIVE : 0),
				remoteFilePath);
		try {
			try {
				int ret = library.ssh_scp_init(scp);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.GENERAL, "Failed to initialised SCP session. Error code " + ret);
				}
				scpReceive(remoteFilePath, scp, destinationFile);
			} finally {
				library.ssh_scp_close(scp);
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.GENERAL, "Failed to retrieve file(s).", ioe);
		} finally {
			library.ssh_scp_free(scp);
		}
	}

	void scpReceive(String remoteFilePath, ssh_scp scp, File destinationFile) throws IOException {
		int ret = 0;
		while (true) {
			ret = library.ssh_scp_pull_request(scp);
			if (ret == SshLibrary.SSH_ERROR) {
				throw new SshException(SshException.GENERAL,
						"Failed to receive pull request. " + library.ssh_get_error(libSshSession.getPointer()));
			} else if (ret == SshLibrary.ssh_scp_request_types.SSH_SCP_REQUEST_EOF) {
				break;
			} else if (ret == SshLibrary.ssh_scp_request_types.SSH_SCP_REQUEST_WARNING) {
				LibsshClient.LOG.log(Level.WARN, library.ssh_scp_request_get_warning(scp));
			} else if (ret == SshLibrary.ssh_scp_request_types.SSH_SCP_REQUEST_ENDDIR) {
				destinationFile = destinationFile.getParentFile();
			} else if (ret == SshLibrary.ssh_scp_request_types.SSH_SCP_REQUEST_NEWDIR) {
				String filename = library.ssh_scp_request_get_filename(scp);
				int mode = library.ssh_scp_request_get_permissions(scp);
				File newDir = new File(destinationFile, filename);
				if (!newDir.mkdirs()) {
					throw new IOException("Failed to create local directory " + newDir);
				}
				Util.setPermissions(newDir, mode);
				library.ssh_scp_accept_request(scp);
				destinationFile = newDir;
			} else if (ret == SshLibrary.ssh_scp_request_types.SSH_SCP_REQUEST_NEWFILE) {
				long size = library.ssh_scp_request_get_size(scp).longValue();
				String filename = library.ssh_scp_request_get_filename(scp);
				int mode = library.ssh_scp_request_get_permissions(scp);
				library.ssh_scp_accept_request(scp);
				Memory m = new Memory(LibsshClient.SCP_BUFFER_SIZE);
				File finalDestination = destinationFile.isDirectory() ? new File(destinationFile, filename) : destinationFile;
				FileOutputStream fout = new FileOutputStream(
						finalDestination);
				fireFileTransferStarted(remoteFilePath, destinationFile.getPath(), size);
				try {
					if (size > 0) {
						long t = 0;
						while (t < size) {
							int read = library.ssh_scp_read(scp, m, new SizeT(m.size()));
							if (read == SshLibrary.SSH_ERROR) {
								throw new IOException("I/O Error");
							}
							byte[] buf = m.getByteArray(0, read);
							fout.write(buf);
							fireFileTransferProgressed(remoteFilePath, destinationFile.getPath(), read);
							t += read;
						}
					}
					else
						/* SCP won't get an EOF in this case so just leave */
						break;
				} finally {
					fout.close();
					Util.setPermissions(finalDestination, mode);
					fireFileTransferFinished(remoteFilePath, destinationFile.getPath());
				}
			}
		}
	}

	@Override
	protected void doPut(String remotePath, String mode, File sourceFile, boolean recursive) throws net.sf.sshapi.SshException {
		checkOpen();
		ssh_scp scp = library.ssh_scp_new(libSshSession, SshLibrary.SSH_SCP_WRITE | (recursive ? SshLibrary.SSH_SCP_RECURSIVE : 0),
				remotePath);
		try {
			try {
				int ret = library.ssh_scp_init(scp);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.GENERAL, "Failed to initialised SCP session. Error code " + ret);
				}
				scpSend(remotePath, scp, sourceFile);
			} finally {
				library.ssh_scp_close(scp);
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.GENERAL, "Failed to send file(s).", ioe);
		} finally {
			library.ssh_scp_free(scp);
		}
	}

	void scpSend(String remotePath, ssh_scp scp, File sourceFile) throws IOException {
		int ret = 0;
		if (sourceFile.isFile()) {
			long sourceLength = sourceFile.length();
			ret = library.ssh_scp_push_file(scp, sourceFile.getName(), new SizeT(sourceLength), LibsshClient.S_IRWXU);
			if (ret != SshLibrary.SSH_OK) {
				throw new IOException("Failed to push file " + sourceFile.getName() + ". Error code " + ret);
			}
			FileInputStream fin = new FileInputStream(sourceFile);
			fireFileTransferStarted(sourceFile.getPath(), remotePath, sourceLength);
			try {
				byte[] buf = new byte[LibsshClient.SCP_BUFFER_SIZE];
				int r = 0;
				long off = 0;
				Memory outBuf = new Memory(Math.max(sourceLength, 1));
				while ((r = fin.read(buf)) != -1) {
					outBuf.write(off, buf, 0, r);
					fireFileTransferProgressed(sourceFile.getPath(), remotePath, r);
					off += r;
				}
				// Hrrmmm, it seems we must write the entire file in one go?
				library.ssh_scp_write(scp, outBuf, new SizeT(sourceLength));
			} finally {
				fin.close();
				fireFileTransferFinished(sourceFile.getPath(), remotePath);
			}
		} else {
			ret = library.ssh_scp_push_directory(scp, sourceFile.getName(), LibsshClient.S_IRWXU);
			if (ret != SshLibrary.SSH_OK) {
				throw new IOException("Failed to push directory. Error code " + ret);
			}
			String[] files = sourceFile.list();
			for (int i = 0; i < files.length; i++) {
				scpSend(remotePath, scp, new File(sourceFile, files[i]));
			}
			ret = library.ssh_scp_leave_directory(scp);
			if (ret != SshLibrary.SSH_OK) {
				throw new IOException("Failed to leave directory. Error code " + ret);
			}
		}
	}
}