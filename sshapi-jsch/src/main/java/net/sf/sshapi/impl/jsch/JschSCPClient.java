package net.sf.sshapi.impl.jsch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractSCPClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshStreamChannel;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.util.Util;

final class JschSCPClient extends AbstractSCPClient {

	private final JschSshClient sshClient;

	/**
	 * @param jschSshClient
	 */
	public JschSCPClient(JschSshClient jschSshClient) {
		sshClient = jschSshClient;
	}

	@Override
	protected void onClose() throws SshException {
		/*
		 * Ganymed opens and closes sessions itself when file operations are performed
		 */
	}

	@Override
	protected void onOpen() throws SshException {
	}

	@Override
	protected void doPut(String remotePath, String mode, File localfile, boolean recursive) throws SshException {
		boolean verbose = false;
		try {
			String command = "scp -p " + (localfile.isDirectory() ? "-d " : "") + "-t " + (recursive ? "-r " : "")
					+ (verbose ? "-v " : "") + remotePath;
			SshConfiguration.getLogger().log(Level.INFO, "Executing command '" + command + "'");
			SshCommand cmd = sshClient.createCommand(command);
			try {
				cmd.open();
				OutputStream out = cmd.getOutputStream();
				InputStream in = cmd.getInputStream();
				int ack = JschSshClient.checkAck(in);
				if (ack != 0) {
					throw new IOException("Incorrect Ack " + ack + " received");
				}
				if (localfile.isDirectory()) {
					File[] f = localfile.listFiles();
					if (f == null) {
						throw new IOException("Could not list local directory " + localfile + ".");
					}

					// Do the files
					for (int i = 0; i < f.length; i++) {
						if (f[i].isFile()) {
							doFile(remotePath, mode, f[i], out, in);
						}
					}

					// Now, if recursive, do the directories
					for (int i = 0; i < f.length; i++) {
						if (f[i].isDirectory()) {
							doPut(remotePath + "/" + f[i].getName(), mode, f[i], recursive);
						}
					}

				} else {
					doFile(remotePath, mode, localfile, out, in);
				}
			} finally {
				cmd.close();
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	private void doFile(String remotePath, String mode, File sourceFile, OutputStream out, InputStream in)
			throws IOException {
		int ack;
		String basename = Util.basename(remotePath);
		String command = "C" + (mode == null ? "0644" : mode) + " " + sourceFile.length() + " " + basename;
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		ack = JschSshClient.checkAck(in);
		if (ack != 0) {
			throw new IOException("Incorrect Ack " + ack + " received");
		}
		byte[] buf = new byte[1024];
		InputStream content = new FileInputStream(sourceFile);
		fireFileTransferStarted(sourceFile.getPath(), remotePath, sourceFile.length());
		try {
			try {
				while (true) {
					int len = content.read(buf, 0, buf.length);
					if (len <= 0)
						break;
					out.write(buf, 0, len); // out.flush();
					fireFileTransferProgressed(sourceFile.getPath(), remotePath, len);
				}
			} finally {
				content.close();
			}
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			ack = JschSshClient.checkAck(in);
			if (ack != 0) {
				throw new IOException("Incorrect Ack " + ack + " received");
			}
		} finally {
			fireFileTransferFinished(sourceFile.getPath(), remotePath);
		}
	}

	@Override
	public void get(final String remoteFilePath, File targetFile, boolean recursive) throws SshException {
		SshStreamChannel<?, ?> cmd = sshClient.createCommand("scp -f " + (recursive ? "-r " : "") + remoteFilePath);
		cmd.open();

		// get I/O streams for remote scp
		try {
			OutputStream out = cmd.getOutputStream();
			InputStream in = cmd.getInputStream();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			boolean run = true;
			while (run) {
				int c = JschSshClient.checkAck(in);
				switch (c) {
				case 'T':
					readLine(in);
				case 'C':
					FileOutputStream fos = new FileOutputStream(targetFile);
					try {
						// read '0644 '
						in.read(buf, 0, 5);

						long filesize = 0L;
						while (true) {
							if (in.read(buf, 0, 1) < 0) {
								// error
								break;
							}
							if (buf[0] == ' ')
								break;
							filesize = filesize * 10L + buf[0] - '0';
						}

						// This is the filename terminated by 0x0a, but we
						// dont
						// really need it
						readLine(in);

						buf[0] = 0;
						out.write(buf, 0, 1);
						out.flush();
						int foo;
						fireFileTransferStarted(remoteFilePath, targetFile.getPath(), filesize);
						try {
							while (true) {
								if (buf.length < filesize)
									foo = buf.length;
								else
									foo = (int) filesize;
								foo = in.read(buf, 0, foo);
								if (foo < 0) {
									// error
									break;
								}
								fos.write(buf, 0, foo);
								fireFileTransferProgressed(remoteFilePath, targetFile.getPath(), foo);
								filesize -= foo;
								if (filesize == 0L)
									break;
							}
						} finally {
							fireFileTransferFinished(remoteFilePath, targetFile.getPath());
						}

						if (JschSshClient.checkAck(in) != 0) {
							throw new IOException("Incorrect Ack received");
						}

						// send '\0'
						buf[0] = 0;
						out.write(buf, 0, 1);
						out.flush();
						break;
					} finally {
						fos.close();
					}
				case 'E':
				case -1:
					run = false;
					break;
				default:
					break;
				}
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		} finally {
			try {
				cmd.close();
			} catch (IOException e) {
				if (e instanceof SshException)
					throw (SshException) e;
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
	}

	private String readLine(InputStream in) throws IOException {
		StringBuffer buf = new StringBuffer();
		for (@SuppressWarnings("unused")
		int i = 0;; i++) {
			int r = in.read();
			if (r == (byte) 0x0a) {
				break;
			}
			buf.append((char) r);
		}
		return buf.toString();
	}
}