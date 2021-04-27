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
package net.sf.sshapi.cli.commands;

import java.util.concurrent.Callable;

import net.sf.sshapi.cli.SftpContainer;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "cd", mixinStandardHelpOptions = true, description = "Change remote directory.")
public class Cd extends SftpCommand implements Callable<Integer> {

    @Parameters(index = "0", arity = "0..1", description = "Directory to change to.")
	private String directory;
    
	public Cd() {
	}

	@Override
	public Integer call() throws Exception {
		SftpContainer container = getContainer();
		SftpClient sftp = container.getClient();

		if (directory != null && directory.length() > 0) {
			directory = translatePath(container.getCwd(), directory);
			try {
				SftpFile file = sftp.stat(directory);
				if (file.isDirectory()) {
					container.setCwd(directory);
				} else {
					System.out.println("Not a directory!");
				}
			} catch (SftpException sftpe) {
				if (sftpe.getCode().equals(SftpException.SSH_FX_NO_SUCH_FILE))
					System.out.println("Directory " + directory + " not found");
			}
		} else {
			container.setCwd(sftp.getDefaultPath());
		}
		return 0;
	}
}