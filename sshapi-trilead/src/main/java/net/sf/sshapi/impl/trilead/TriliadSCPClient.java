package net.sf.sshapi.impl.trilead;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.trilead.ssh2.SCPClient;

import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.util.Util;

final class TriliadSCPClient extends AbstractSCPClient implements SshSCPClient {

	/**
	 * 
	 */
	private final TrileadSshClient ganymedSshClient;

	/**
	 * @param ganymedSshClient
	 */
	TriliadSCPClient(TrileadSshClient ganymedSshClient) {
		this.ganymedSshClient = ganymedSshClient;
	}

	private SCPClient client;

	protected void onClose() throws SshException {
		/*
		 * Ganymed opens and closes sessions itself when file operations are
		 * performed
		 */
	}

	protected void onOpen() throws SshException {
		try {
			client = this.ganymedSshClient.connection.createSCPClient();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	protected void doPut(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		try {
			if (localfile.isDirectory()) {
				File[] f = localfile.listFiles();
				if (f == null) {
					throw new IOException("Could not list local directory " + localfile + ".");
				}
				for (int i = 0; i < f.length; i++) {
					if (recursive || f[i].isFile()) {
						doPut(remotePath + "/" + f[i].getName(), mode, f[i], recursive);
					}
				}
			} else {
				fireFileTransferStarted(localfile.getPath(), remotePath, localfile.length());
				try {
					client.put(localfile.getPath(), remotePath, mode == null ? "0600" : mode);
				} finally {
					fireFileTransferFinished(localfile.getPath(), remotePath);
				}
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	public void get(final String remotePath, File targetFile, boolean recursive)
			throws SshException {
		if (recursive) {
			throw new UnsupportedOperationException(
					"Ganymed does not support recursively retrieving files from the server using SCP");
		}
		try {
			if (targetFile.isDirectory()) {
				FileOutputStream fout = new FileOutputStream(new File(targetFile, Util.basename(remotePath)));
				try {
					client.get(remotePath, fout);
				}
				finally {
					fout.close();
				}
			} else {
				FileOutputStream fout = new FileOutputStream(targetFile);
				try {
					client.get(remotePath, fout);
				}
				finally {
					fout.close();
				}
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		int len;
		while ((len = in.read(buffer)) != -1) {
		    out.write(buffer, 0, len);
		}
	}
}