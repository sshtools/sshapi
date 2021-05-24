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
package net.sf.sshapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.util.Util;

/**
 * A default SCP implementation that can be used for providers that don't implement SCP themselves.
 */
public class DefaultSCPClient extends AbstractSCPClient {
	private static final Logger LOG = SshConfiguration.getLogger();
	private final SshClient sshClient;

	/**
	 * Constructor.
	 * 
	 * @param jschSshClient client
	 */
	public DefaultSCPClient(SshClient jschSshClient) {
		super(jschSshClient.getProvider());
		sshClient = jschSshClient;
	}

	@Override
	protected void onClose() throws SshException {
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
			LOG.debug("Executing command '{0}'", command);
			SshCommand cmd = sshClient.createCommand(command);
			try {
				cmd.open();
				OutputStream out = cmd.getOutputStream();
				InputStream in = cmd.getInputStream();
				checkAck(in);
				LOG.debug("First acknowledge SCP {0} (mode {1}) from {2}", remotePath, mode, localfile);
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
					LOG.debug("Completed files SCP {0} (mode {1}) from {2}", remotePath, mode, localfile);
					// Now, if recursive, do the directories
					for (int i = 0; i < f.length; i++) {
						if (f[i].isDirectory()) {
							doPut(remotePath + "/" + f[i].getName(), mode, f[i], recursive);
						}
					}
					LOG.debug("Completed folders SCP {0} (mode {1}) from {2}", remotePath, mode, localfile);
				} else {
					doFile(remotePath, mode, localfile, out, in);
				}
			} finally {
				LOG.debug("Closing command for SCP {0} (mode {1}) from {2}", remotePath, mode, localfile);
				cmd.close();
				LOG.debug("Closed command for SCP {0} (mode {1}) from {2}", remotePath, mode, localfile);
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	private void doFile(String remotePath, String mode, File sourceFile, OutputStream out, InputStream in) throws IOException {
		LOG.debug("Transferring file {0} (mode {1}) from {2}", remotePath, mode, sourceFile);
		String basename = Util.basename(remotePath);
		String command = "C" + (mode == null ? "0644" : mode) + " " + sourceFile.length() + " " + basename;
		command += "\n";
		out.write(command.getBytes());
		out.flush();
		checkAck(in);
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
			LOG.debug("Finished transferring file {0} (mode {1}) from {2}", remotePath, mode, sourceFile);
			checkAck(in);
			LOG.debug("Acknowledge transferring file {0} (mode {1}) from {2}", remotePath, mode, sourceFile);
		} finally {
			fireFileTransferFinished(sourceFile.getPath(), remotePath);
		}
	}

	@Override
	public void get(final String remoteFilePath, File targetFile, boolean recursive) throws SshException {
		LOG.debug("SCPd for file {0} (mode {1})", remoteFilePath, targetFile);
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
		LOG.debug("Send first ACK for file {0} (mode {1})", remoteFilePath, targetFile);
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		boolean run = true;
		while (run) {
			int c = readAck(in);
			LOG.debug("Ack {0} ({1}) for file {2} (mode {3})", c, Character.valueOf((char) c), remoteFilePath, targetFile);
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
					LOG.debug("File {0} is {1} bytes", remoteFilePath, filesize);
					while (left > 0) {
						int len = in.read(buf, 0, (int)Math.min(buf.length, left));
						if (len <= 0)
							break;
						left -= len;
						if(LOG.isTrace())
							LOG.trace("Read block of {0} from file {1}, {2} left", len, filesize, left);
						fos.write(buf, 0, len); 
						fos.flush();
						fireFileTransferProgressed(remoteFilePath, target.getPath(), filesize - left);
					}
					LOG.debug("Completed File {0} is {1} bytes", remoteFilePath, filesize);
				}
				
				LOG.debug("Completed for file {0} (mode {1})", remoteFilePath, target);
				fireFileTransferFinished(remoteFilePath, target.getPath());
				
				checkAck(in);

				// send '\0'
				LOG.debug("Send ACK for file {0} (mode {1})", remoteFilePath, target);
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
				break;
			case 'E':
				LOG.debug("End, sending ACK for file {0}", remoteFilePath);
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
	
	static void checkAck(InputStream in) throws IOException {
		int ack = readAck(in);
		if(ack != 0) {
			throw new IOException(String.format("Unexpected acknowledgement %d", ack));
		}
	}
	
	static int readAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;
		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				throw new IOException(String.format("Error. %s", sb.toString()));
			}
			if (b == 2) { // fatal error
				throw new IOException(String.format("Fatal Error. %s", sb.toString()));
			}
		}
		return b;
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