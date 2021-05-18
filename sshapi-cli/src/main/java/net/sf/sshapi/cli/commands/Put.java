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

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.Callable;

import net.sf.sshapi.cli.SftpContainer;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.Util;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * Put file command.
 */
@Command(name = "put", mixinStandardHelpOptions = true, description = "Upload local file.")
public class Put extends SftpCommand implements Callable<Integer> {

	@Parameters(index = "0", arity = "1", description = "File to store.")
	private String file;

	@Override
	public Integer call() throws Exception {
		SftpContainer container = getContainer();
		SftpClient sftp = container.getClient();
		String base = translatePath(container.getCwd(), Util.basename(file));
		FileInputStream fin = new FileInputStream(new File(container.getLcwd(), file));
		try {
			sftp.put(base, fin, 0644);
		} finally {
			fin.close();
		}
		container.getTerminal().writer().println(String.format("Uploaded %s to %s", file, base));
		return 0;
	}
}