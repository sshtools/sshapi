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

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.fuse.fs.FuseSFTP;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Command line tool to mount an SFTP server as a native file system (via FUSE).
 */
@Command(name = "sftpmount", mixinStandardHelpOptions = true, description = "Mount remote filesystem.")
public class sftpmount extends sftp implements Logger, Callable<Integer> {

	@Option(names = { "-D", "--drive-name" }, description = "Drive name.")
	protected String driveName;

	@Parameters(index = "1", description = "Mount point.")
	protected Path mount;

	/**
	 * Entry point.
	 * 
	 * @param args command line arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		sftpmount client = new sftpmount();
		System.exit(new CommandLine(client).execute(args));
	}

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public sftpmount() throws SshException {
	}

	@Override
	public Integer call() throws SshException {
		try {
			start();
		} catch (SshException sshe) {
			if (sshe.getCode().equals(SshException.HOST_KEY_REJECTED)) {
				// Already displayed a message, just exit
			} else {
				System.err.println("ssh: " + sshe.getMessage());
				if (!isQuiet())
					error("Failed to connect to sftp server.", sshe);
			}
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ssh: " + e.getMessage());
			return 1;
		}
		return 0;
	}

	@Override
	protected boolean isBatchMode() {
		return false;
	}

	protected void onStart() throws SshException, IOException {
		configuration.setIoTimeout(0);
		int idx = destination.indexOf(':');
		if (idx != -1) {
			setCwd(destination.substring(idx + 1));
			destination = destination.substring(0, idx);
		}
		try (SshClient client = connect(destination)) {
			sftp = client.sftp();
			try {
				SshConfiguration.getLogger().info("Starting at {0}", sftp.getDefaultPath());
				try (FuseSFTP fuseFs = new FuseSFTP(sftp)) {
					fuseFs.mount(mount, true, true, new String[0]
					/*
					 * new String[] { "-o", "volname=" + ( driveName == null ? "DRV1" : driveName )
					 * }
					 */);
				}

			} finally {
				sftp.close();
			}
		}
	}
}
