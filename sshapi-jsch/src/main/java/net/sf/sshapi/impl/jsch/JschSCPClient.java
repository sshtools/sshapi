package net.sf.sshapi.impl.jsch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

class JschSCPClient extends AbstractSCPClient {
	private final JschSshClient sshClient;

	/**
	 * @param jschSshClient
	 */
	public JschSCPClient(JschSshClient jschSshClient) {
		super(jschSshClient.getProvider());
		sshClient = jschSshClient;
	}

	@Override
	protected void onClose() throws SshException {
		/*
		 * Ganymed opens and closes sessions itself when file operations are
		 * performed
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
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("Frist acknowledge SCP %s (mode %s) from %s", remotePath, mode, localfile));
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
					SshConfiguration.getLogger().log(Level.INFO,
							String.format("Completed files SCP %s (mode %s) from %s", remotePath, mode, localfile));
					// Now, if recursive, do the directories
					for (int i = 0; i < f.length; i++) {
						if (f[i].isDirectory()) {
							doPut(remotePath + "/" + f[i].getName(), mode, f[i], recursive);
						}
					}
					SshConfiguration.getLogger().log(Level.INFO,
							String.format("Completed folders SCP %s (mode %s) from %s", remotePath, mode, localfile));
				} else {
					doFile(remotePath, mode, localfile, out, in);
				}
			} finally {
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("Closing command for SCP %s (mode %s) from %s", remotePath, mode, localfile));
				cmd.close();
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("Closed command for SCP %s (mode %s) from %s", remotePath, mode, localfile));
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	private void doFile(String remotePath, String mode, File sourceFile, OutputStream out, InputStream in) throws IOException {
		int ack;
		SshConfiguration.getLogger().log(Level.INFO,
				String.format("Transferring file %s (mode %s) from %s", remotePath, mode, sourceFile));
		String basename = Util.basename(remotePath);
		String command = "C" + (mode == null ? "0644" : mode) + " " + sourceFile.length() + " " + basename;
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		ack = JschSshClient.checkAck(in);
		if (ack != 0) {
			throw new IOException("Incorrect Ack " + ack + " received");
		}
		byte[] buf = new byte[sshClient.getConfiguration().getStreamBufferSize() == 0 ? 32768 :(int)sshClient.getConfiguration().getStreamBufferSize()];
		try (InputStream content = new FileInputStream(sourceFile)) {
			fireFileTransferStarted(sourceFile.getPath(), remotePath, sourceFile.length());
			while (true) {
				int len = content.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
				fireFileTransferProgressed(sourceFile.getPath(), remotePath, len);
			}
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			SshConfiguration.getLogger().log(Level.INFO,
					String.format("Finishgd transferring file %s (mode %s) from %s", remotePath, mode, sourceFile));
			ack = JschSshClient.checkAck(in);
			if (ack != 0) {
				throw new IOException("Incorrect Ack " + ack + " received");
			}
			SshConfiguration.getLogger().log(Level.INFO,
					String.format("Acknowledge transferring file %s (mode %s) from %s", remotePath, mode, sourceFile));
		} finally {
			fireFileTransferFinished(sourceFile.getPath(), remotePath);
		}
	}

	@Override
	public void get(final String remoteFilePath, File targetFile, boolean recursive) throws SshException {
		SshConfiguration.getLogger().log(Level.INFO, String.format("SCPd for file %s (mode %s)", remoteFilePath, targetFile));
		SshStreamChannel<?, ?> cmd = sshClient.createCommand("scp -f " + (recursive ? "-r " : "") + remoteFilePath);
		cmd.open();
		// get I/O streams for remote scp
		try {
			OutputStream out = cmd.getOutputStream();
			InputStream in = cmd.getInputStream();
			byte[] buf = new byte[sshClient.getConfiguration().getStreamBufferSize() == 0 ? 32768 :(int)sshClient.getConfiguration().getStreamBufferSize()];
			// send '\0'
			doGet(remoteFilePath, targetFile, cmd, out, in, buf);
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

	private boolean doGet(final String remoteFilePath, File targetFile, SshStreamChannel<?, ?> cmd, OutputStream out, InputStream in,
			byte[] buf) throws IOException, SshException, FileNotFoundException {
		SshConfiguration.getLogger().log(Level.INFO,
				String.format("Send first ACK for file %s (mode %s)", remoteFilePath, targetFile));
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		boolean run = true;
		while (run) {
			int c = JschSshClient.checkAck(in);
			SshConfiguration.getLogger().log(Level.INFO,
					String.format("Ack %d (%s) for file %s (mode %s)", c, Character.valueOf((char) c), remoteFilePath, targetFile));
			switch (c) {
			case 'T':
				continue;
			case 'C':
			case 'D':
				String targetName = targetFile.getAbsolutePath();
				String[] parts = parseCommand(readLine(in));
				if (targetFile.isDirectory()) {
					targetName += (File.separator + parts[2]);
				}
				File target = new File(targetName);
				if (c == 'D') {
					if (target.exists()) {
						if (!target.isDirectory()) {
							throw new SshException(String.format("Invalid target %s must be a directory", targetFile));
						}
					} else {
						if (!target.mkdir()) {
							throw new SshException(String.format("Could not create directory %s", targetFile));
						}
					}
					if(doGet(remoteFilePath, target, cmd, out, in, buf))						
						continue;
					else
						return false;
				}
				long filesize = Long.parseLong(parts[1]);
				long left = filesize;
				try(FileOutputStream fos = new FileOutputStream(target)) {
					buf[0] = 0;
					out.write(buf, 0, 1);
					out.flush();	
					fireFileTransferStarted(remoteFilePath, target.getPath(), filesize);
					SshConfiguration.getLogger().log(Level.INFO,
							String.format("File %s is %d bytes", remoteFilePath, filesize));
					while (left > 0) {
						int len = in.read(buf, 0, (int)Math.min(buf.length, left));
						if (len <= 0)
							break;
						left -= len;
						SshConfiguration.getLogger().log(Level.INFO,
								String.format("Read block of %d from file %s, %d left", len, filesize, left));
						fos.write(buf, 0, len); 
						fos.flush();
						fireFileTransferProgressed(remoteFilePath, target.getPath(), filesize - left);
					}
					SshConfiguration.getLogger().log(Level.INFO,
							String.format("Completed File %s is %d bytes", remoteFilePath, filesize));
				}
				
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("Completed for file %s (mode %s)", remoteFilePath, target));
				fireFileTransferFinished(remoteFilePath, target.getPath());
				
				if (JschSshClient.checkAck(in) != 0) {
					throw new IOException("Incorrect Ack received");
				}
				// send '\0'
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("Send ACK for file %s (mode %s)", remoteFilePath, target));
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
				break;
			case 'E':
				SshConfiguration.getLogger().log(Level.INFO,
						String.format("End, sending ACK for file %s", remoteFilePath));
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
				return false;
			case -1:
				run = false;
				break;
			default:
				throw new SshException(String.format("Unexpected command. %d (%s)", c, (char)c));
			}
		}
		return true;
	}

	private String[] parseCommand(String cmd) throws IOException {
		int l = cmd.indexOf(' ');
		int r = cmd.indexOf(' ', l + 1);
		if ((l == -1) || (r == -1)) {
			throw new IllegalArgumentException(String.format("Unexpected SCP command %s", cmd));
		}
		return new String[] { cmd.substring(1, l), cmd.substring(l + 1, r), cmd.substring(r + 1) };
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