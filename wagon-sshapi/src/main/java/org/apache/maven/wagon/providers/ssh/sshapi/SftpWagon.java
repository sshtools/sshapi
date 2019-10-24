package org.apache.maven.wagon.providers.ssh.sshapi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.PathUtils;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.providers.ssh.ScpHelper;
import org.apache.maven.wagon.repository.RepositoryPermissions;
import org.apache.maven.wagon.resource.Resource;

import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpFile;

/**
 * SFTP protocol wagon via SSHAPI. Based on the JSch SFTP Wagon.
 * 
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: SftpWagon.java 1172843 2011-09-19 21:28:04Z hboutemy $
 * @todo [BP] add compression flag
 * @todo see if SftpProgressMonitor allows us to do streaming (without it, we
 *       can't do checksums as the input stream is lost)
 * 
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="sftp"
 *                   instantiation-strategy="per-lookup"
 */
public class SftpWagon extends AbstractSSHAPIWagon {

	private SftpClient channel;
	private String remotePath = "/";

	public void closeConnection() {
		if (channel != null) {
			try {
				channel.close();
			} catch (IOException sshe) {
			}
		}
		super.closeConnection();
	}

	public void openConnectionInternal() throws AuthenticationException, ConnectionException {
		super.openConnectionInternal();

		try {
			channel = session.createSftp();
			channel.open();
		} catch (SshException e) {
			throw new ConnectionException("Error connecting to remote repository: " + getRepository().getUrl(), e);
		}
	}

	private void returnToParentDirectory(Resource resource) {
		try {
			String dir = ScpHelper.getResourceDirectory(resource.getName());
			String[] dirs = PathUtils.dirnames(dir);
			for (int i = 0; i < dirs.length; i++) {
				cd("..");
			}
		} catch (SshException e) {
			fireTransferDebug("Error returning to parent directory: " + e.getMessage());
		}
	}

	private void putFile(File source, Resource resource, RepositoryPermissions permissions)
			throws TransferFailedException, IOException, SshException {
		resource.setContentLength(source.length());

		resource.setLastModified(source.lastModified());

		String filename = ScpHelper.getResourceFilename(resource.getName());

		firePutStarted(resource, source);

		FileInputStream fin = new FileInputStream(source);
		try {
			channel.put(filename, fin, -1);
		} finally {
			fin.close();
		}

		postProcessListeners(resource, source, TransferEvent.REQUEST_PUT);

		if (permissions != null && permissions.getGroup() != null) {
			setGroup(filename, permissions);
		}

		if (permissions != null && permissions.getFileMode() != null) {
			setFileMode(filename, permissions);
		}

		firePutCompleted(resource, source);
	}

	private void setGroup(String filename, RepositoryPermissions permissions) {
		try {
			int group = Integer.valueOf(permissions.getGroup()).intValue();
			channel.chgrp(filename, group);
		} catch (NumberFormatException e) {
			// TODO: warning level
			fireTransferDebug("Not setting group: must be a numerical GID for SFTP");
		} catch (SshException e) {
			fireTransferDebug("Not setting group: " + e.getMessage());
		}
	}

	private void setFileMode(String filename, RepositoryPermissions permissions) {
		try {
			int mode = getOctalMode(permissions.getFileMode());
			channel.chmod(filename, mode);
		} catch (NumberFormatException e) {
			// TODO: warning level
			fireTransferDebug("Not setting mode: must be a numerical mode for SFTP");
		} catch (SshException e) {
			fireTransferDebug("Not setting mode: " + e.getMessage());
		}
	}

	private void mkdirs(String resourceName, int mode) throws SshException, TransferFailedException {
		String[] dirs = PathUtils.dirnames(resourceName);
		for (int i = 0; i < dirs.length; i++) {
			mkdir(dirs[i], mode);
			cd(dirs[i]);
		}
	}

	private void mkdir(String dir, int mode) throws TransferFailedException, SshException {
		try {
			SftpFile attrs = channel.stat(dir);
			if (!attrs.isDirectory()) {
				throw new TransferFailedException("Remote path is not a directory:" + dir);
			}
		} catch (SshException e) {
			// doesn't exist, make it and try again
			channel.mkdir(dir, mode);
		}
	}

	private SftpFile changeToRepositoryDirectory(String dir, String filename)
			throws ResourceDoesNotExistException, SshException {
		// This must be called first to ensure that if the file doesn't exist it
		// throws an exception
		SftpFile attrs;
		try {
			cd(repository.getBasedir());

			if (dir.length() > 0) {
				cd(dir);
			}

			if (filename.length() == 0) {
				filename = ".";
			}

			attrs = channel.stat(filename);
		} catch (SshException e) {
			if (e.toString().trim().endsWith("No such file")) {
				throw new ResourceDoesNotExistException(e.toString(), e);
			} else if (e.toString().trim().indexOf("Can't change directory") != -1) {
				throw new ResourceDoesNotExistException(e.toString(), e);
			} else {
				throw e;
			}
		}
		return attrs;
	}

	public void putDirectory(File sourceDirectory, String destinationDirectory)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		final RepositoryPermissions permissions = repository.getPermissions();

		try {
			cd("/");

			String basedir = getRepository().getBasedir();
			int directoryMode = getDirectoryMode(permissions);

			mkdirs(basedir + "/", directoryMode);

			fireTransferDebug("Recursively uploading directory " + sourceDirectory.getAbsolutePath() + " as "
					+ destinationDirectory);

			mkdirs(destinationDirectory, directoryMode);
			ftpRecursivePut(sourceDirectory, null, ScpHelper.getResourceFilename(destinationDirectory), directoryMode);
		} catch (SshException e) {
			String msg = "Error occurred while deploying '" + sourceDirectory.getAbsolutePath() + "' "
					+ "to remote repository: " + getRepository().getUrl() + ": " + e.getMessage();

			throw new TransferFailedException(msg, e);
		} catch (IOException e) {
			String msg = "Error occurred while deploying '" + sourceDirectory.getAbsolutePath() + "' "
					+ "to remote repository: " + getRepository().getUrl() + ": " + e.getMessage();

			throw new TransferFailedException(msg, e);
		}
	}

	private void ftpRecursivePut(File sourceFile, String prefix, String fileName, int directoryMode)
			throws TransferFailedException, SshException, ResourceDoesNotExistException, IOException {
		final RepositoryPermissions permissions = repository.getPermissions();

		if (sourceFile.isDirectory()) {
			if (!fileName.equals(".")) {
				prefix = getFileName(prefix, fileName);
				mkdir(fileName, directoryMode);
				cd(fileName);
			}

			File[] files = sourceFile.listFiles();
			if (files != null && files.length > 0) {
				// Directories first, then files. Let's go deep early.
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						ftpRecursivePut(files[i], prefix, files[i].getName(), directoryMode);
					}
				}
				for (int i = 0; i < files.length; i++) {
					if (!files[i].isDirectory()) {
						ftpRecursivePut(files[i], prefix, files[i].getName(), directoryMode);
					}
				}
			}

			cd("..");
		} else {
			Resource resource = ScpHelper.getResource(getFileName(prefix, fileName));

			firePutInitiated(resource, sourceFile);

			try {
				putFile(sourceFile, resource, permissions);
			} catch (FileNotFoundException fnfe) {
				throw new ResourceDoesNotExistException(fnfe.getLocalizedMessage());
			}
		}
	}

	private String getFileName(String prefix, String fileName) {
		if (prefix != null) {
			prefix = prefix + "/" + fileName;
		} else {
			prefix = fileName;
		}
		return prefix;
	}

	public List getFileList(String destinationDirectory)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		if (destinationDirectory.length() == 0) {
			destinationDirectory = ".";
		}

		String filename = ScpHelper.getResourceFilename(destinationDirectory);

		String dir = ScpHelper.getResourceDirectory(destinationDirectory);

		// we already setuped the root directory. Ignore beginning /
		if (dir.length() > 0 && dir.charAt(0) == ScpHelper.PATH_SEPARATOR) {
			dir = dir.substring(1);
		}

		try {
			SftpFile attrs = changeToRepositoryDirectory(dir, filename);
			if (!attrs.isDirectory()) {
				throw new TransferFailedException("Remote path is not a directory:" + dir);
			}

			SftpFile[] fileList = channel.ls(filename);
			List files = new ArrayList(fileList.length);
			for (int i = 0; i < fileList.length; i++) {
				String name = fileList[i].getName();
				if (fileList[i].isDirectory()) {
					if (!name.equals(".") && !name.equals("..")) {
						if (!name.endsWith("/")) {
							name += "/";
						}
						files.add(name);
					}
				} else {
					files.add(name);
				}
			}
			return files;
		} catch (SshException e) {
			String msg = "Error occurred while listing '" + destinationDirectory + "' " + "on remote repository: "
					+ getRepository().getUrl() + ": " + e.getMessage();

			throw new TransferFailedException(msg, e);
		}
	}

	public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
		String filename = ScpHelper.getResourceFilename(resourceName);

		String dir = ScpHelper.getResourceDirectory(resourceName);

		// we already setuped the root directory. Ignore beginning /
		if (dir.length() > 0 && dir.charAt(0) == ScpHelper.PATH_SEPARATOR) {
			dir = dir.substring(1);
		}

		try {
			changeToRepositoryDirectory(dir, filename);
			return true;
		} catch (ResourceDoesNotExistException e) {
			return false;
		} catch (SshException e) {
			String msg = "Error occurred while looking for '" + resourceName + "' " + "on remote repository: "
					+ getRepository().getUrl() + ": " + e.getMessage();

			throw new TransferFailedException(msg, e);
		}
	}

	protected void cleanupGetTransfer(Resource resource) {
		returnToParentDirectory(resource);
	}

	protected void cleanupPutTransfer(Resource resource) {
		returnToParentDirectory(resource);
	}

	protected void finishPutTransfer(Resource resource, InputStream input, OutputStream output)
			throws TransferFailedException {
		RepositoryPermissions permissions = getRepository().getPermissions();

		String filename = ScpHelper.getResourceFilename(resource.getName());
		if (permissions != null && permissions.getGroup() != null) {
			setGroup(filename, permissions);
		}

		if (permissions != null && permissions.getFileMode() != null) {
			setFileMode(filename, permissions);
		}
	}

	public void fillInputData(InputData inputData) throws TransferFailedException, ResourceDoesNotExistException {
		Resource resource = inputData.getResource();

		String filename = ScpHelper.getResourceFilename(resource.getName());

		String dir = ScpHelper.getResourceDirectory(resource.getName());

		// we already setuped the root directory. Ignore beginning /
		if (dir.length() > 0 && dir.charAt(0) == ScpHelper.PATH_SEPARATOR) {
			dir = dir.substring(1);
		}

		try {
			SftpFile attrs = changeToRepositoryDirectory(dir, filename);
			long lastModified = attrs.getLastModified();
			resource.setContentLength(attrs.getSize());
			resource.setLastModified(lastModified);
			inputData.setInputStream(channel.get(filename));
		} catch (SshException e) {
			handleGetException(resource, e);
		}
	}

	public void fillOutputData(OutputData outputData) throws TransferFailedException {
		int directoryMode = getDirectoryMode(getRepository().getPermissions());

		Resource resource = outputData.getResource();

		try {
			cd("/");
			String basedir = getRepository().getBasedir();
			mkdirs(basedir + "/", directoryMode);
			mkdirs(resource.getName(), directoryMode);
			String filename = ScpHelper.getResourceFilename(resource.getName());
			outputData.setOutputStream(channel.put(filename, -1));
		} catch (TransferFailedException e) {
			fireTransferError(resource, e, TransferEvent.REQUEST_PUT);

			throw e;
		} catch (SshException e) {
			fireTransferError(resource, e, TransferEvent.REQUEST_PUT);

			String msg = "Error occurred while deploying '" + resource.getName() + "' " + "to remote repository: "
					+ getRepository().getUrl() + ": " + e.getMessage();

			throw new TransferFailedException(msg, e);
		}
	}

	/**
	 * @param permissions
	 *            repository's permissions
	 * @return the directory mode for the repository or <code>-1</code> if it wasn't
	 *         set
	 */
	public int getDirectoryMode(RepositoryPermissions permissions) {
		int ret = -1;

		if (permissions != null) {
			ret = getOctalMode(permissions.getDirectoryMode());
		}

		return ret;
	}

	/**
	 * Get the integer permission value given an octal mode string.
	 * 
	 * @param mode
	 *            mode string
	 * @return integer permission value
	 */
	public int getOctalMode(String mode) {
		int ret;
		try {
			ret = Integer.valueOf(mode, 8).intValue();
		} catch (NumberFormatException e) {
			// TODO: warning level
			fireTransferDebug("the file mode must be a numerical mode for SFTP");
			ret = -1;
		}
		return ret;
	}

	private void cd(String remotePath) throws SshException {
		fireSessionDebug("Changing directory to " + remotePath + " from " + this.remotePath);
		String newRemotePath = this.remotePath;
		if (remotePath.equals("..")) {
			newRemotePath = PathUtils.dirname(remotePath);
		} else if (remotePath.equals(".")) {

		} else if (remotePath.startsWith("/")) {
			newRemotePath = remotePath;
		} else {
			newRemotePath = (newRemotePath.equals("/") ? "" : newRemotePath) + "/" + remotePath;
		}
		SftpFile remoteWorkingDirectory = channel.stat(this.remotePath);
		if (!remoteWorkingDirectory.isDirectory()) {
			throw new SshException("Not a directory.");
		}
		this.remotePath = newRemotePath;
		fireSessionDebug("New remote directory is " + this.remotePath);
	}
}
