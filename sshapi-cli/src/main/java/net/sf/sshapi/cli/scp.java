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
package net.sf.sshapi.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshSCPClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Java clone of the de-facto standard OpenSSH ssh command.
 */
@Command(name = "scp", mixinStandardHelpOptions = true, description = "Secure file copy.")
public class scp extends AbstractSshFilesCommand implements Logger, Callable<Integer> {
	
	/**
	 * Entry point.
	 *
	 * @param args command line arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		scp cli = new scp();
		System.exit(new CommandLine(cli).execute(args));
	}
	
	@Option(names = { "-B",
			"--batch" }, description = "Selects batch mode (prevents asking for passwords or passphrases).")
	private boolean batch;
	
	@Option(names = { "-P", "--port" }, description = "Port number on which the server is listening.")
	private int port = 22;
	
	@Option(names = { "-p",
			"--preserve-attributes" }, description = "Preserves modification times, access times, and modes from the original file..")
	private boolean preserveAttributes;
	
	@Option(names = { "-r",
			"--recursive" }, description = " Recursively copy entire directories.  Note that ssh follows symbolic links encountered in the tree traversal.")
	private boolean recursive;
	
	@Parameters(index = "0", description = "The source file or directory.")
	private String source;

	@Parameters(index = "1", description = "The target file or directory.")
	private String target;


	/**
	 * Constructor.
	 *
	 * @throws SshException
	 */
	public scp() throws SshException {
	}

	@Override
	public Integer call() throws SshException {
		try {
			start();
		} catch (FileNotFoundException e) {
			if (!isQuiet()) {
				System.err.println(e.getMessage() + ": No such file or directory");
			}
			return 1;
		} catch (SshException sshe) {
			if (sshe.getCode().equals(SshException.HOST_KEY_REJECTED)) {
				// Already displayed a message, just exit
			} else {
				if (!isQuiet()) {
					System.err.println("scp: " + sshe.getMessage());
					if(isLevelEnabled(Level.DEBUG))
						sshe.printStackTrace();
				}
			}
			return 1;
		} catch (Exception e) {
			if (!isQuiet()) {
				System.err.println("scp: " + e.getMessage());
				if(isLevelEnabled(Level.DEBUG))
					e.printStackTrace();
			}
			return 1;
		}
		return 0;
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return this.level.compareTo(level) <= 0;
	}

	@Override
	protected int getPort() {
		return port;
	}

//	void buildOptions(Options options) {
//		options.addOption("3", false,
//				"Copies between two remote hosts are transferred through the local host.  Without "
//						+ "this option the data is copied directly between the two remote hosts.  Note that this option disables "
//						+ "the progress meter.");
//		options.addOption("F", true,
//				"Specifies an alternative per-user configuration file (IGNORED - Only present for OpenSSH compatibility).");
//		options.addOption("o", true, "Additional options (IGNORED - Only present for OpenSSH compatibility).");
//		options.addOption("4", false,
//				"Forces use of IPv4 addresses only (IGNORED - Only present for OpenSSH compatibility).");
//		options.addOption("6", false,
//				"Forces use of IPv6 addresses only (IGNORED - Only present for OpenSSH compatibility).");
//		options.addOption("l", false,
//				"Limits the used bandwidth, specified in Kbit/s. (IGNORED - Only present for OpenSSH compatibility).");
//	}

	@Override
	protected boolean isBatchMode() {
		return batch;
	}

	@Override
	protected void onStart() throws SshException, IOException {
		if (isRemotePath(source)) {
			if (isRemotePath(target)) {
				// Remote to remote
				remoteToRemote();
			} else {
				// Remote to local
				remoteToLocal();
			}
		} else {
			if (isRemotePath(target)) {
				localToRemote();
				// Local to remote
			} else {
				// Local to local
				throw new UnsupportedOperationException("TODO");
			}
		}
	}

	File checkPath(String path) throws FileNotFoundException {
		File file = new File(path);
		if (!file.exists()) {
			throw new FileNotFoundException(path);
		}
		return file;
	}

	@Override
	String getPath(String path) {
		int idx = path.indexOf(":");
		if (idx == -1) {
			return null;
		}
		return path.substring(idx + 1);
	}

	@Override
	boolean isRemotePath(String path) {
		int idx = path.indexOf("@");
		int idx2 = path.indexOf(":");
		return idx > -1 && idx2 > -1 && idx2 > idx;
	}

	void localToRemote() throws SshException, IOException {
		String targetPath = getPath(target);
		try (SshClient client = connect(getConnectionDetails(target))) {
			SshSCPClient scp = client.createSCP();
			if(provider.getCapabilities().contains(Capability.SCP_CAN_PRESERVE_ATTRIBUTES))
				scp.setPreserveAttributes(preserveAttributes);
			scp.addFileTransferListener(this);
			scp.open();
			try {
				// TODO preserve modes in SFTP client as well
				scp.put(targetPath, "0770", checkPath(source), recursive);
			} finally {
				scp.close();
			}
		}
	}

	void remoteToLocal() throws SshException, IOException {
		connect(getConnectionDetails(source));
	}

	void remoteToRemote() throws SshException, IOException {
		connect(getConnectionDetails(source));
		connect(getConnectionDetails(target));
	}
}
