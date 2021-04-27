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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;

import net.sf.sshapi.SshClient;

/**
 * Commons VFS file provider implementation for SFTP via SSHAPI
 */
public class SftpFileProvider extends AbstractOriginatingFileProvider {

	protected final static Collection<org.apache.commons.vfs2.Capability> capabilities = Collections.unmodifiableCollection(
			Arrays.asList(new org.apache.commons.vfs2.Capability[] { org.apache.commons.vfs2.Capability.CREATE,
					org.apache.commons.vfs2.Capability.ATTRIBUTES, org.apache.commons.vfs2.Capability.DELETE,
					org.apache.commons.vfs2.Capability.RENAME, org.apache.commons.vfs2.Capability.GET_TYPE,
					org.apache.commons.vfs2.Capability.LIST_CHILDREN, org.apache.commons.vfs2.Capability.READ_CONTENT,
					org.apache.commons.vfs2.Capability.URI, org.apache.commons.vfs2.Capability.WRITE_CONTENT,
					org.apache.commons.vfs2.Capability.GET_LAST_MODIFIED,
					org.apache.commons.vfs2.Capability.SET_LAST_MODIFIED_FILE,
					org.apache.commons.vfs2.Capability.RANDOM_ACCESS_READ }));

	/**
	 * Constructor.
	 */
	public SftpFileProvider() {
		super();
		setFileNameParser(SftpFileNameParser.getInstance());
	}

	protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
			throws FileSystemException {

		// Create the file system
		final GenericFileName rootName = (GenericFileName) name;

		SshClient ssh;
		try {
			ssh = SshClientFactory.createConnection(rootName.getHostName(), rootName.getPort(), rootName.getUserName(),
					rootName.getPassword(), fileSystemOptions);
		} catch (final Exception e) {
			throw new FileSystemException("vfs.provider.sftp/connect.error", name, e);
		}

		return new SftpFileSystem(rootName, ssh, fileSystemOptions);
	}

	public void init() throws FileSystemException {
	}

	public FileSystemConfigBuilder getConfigBuilder() {
		return SftpFileSystemConfigBuilder.getInstance();
	}

	public Collection<Capability> getCapabilities() {
		return capabilities;
	}
}
