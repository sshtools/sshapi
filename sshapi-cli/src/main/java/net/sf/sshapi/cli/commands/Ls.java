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

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import net.sf.sshapi.cli.SftpContainer;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.Util;
import picocli.CommandLine.Command;

/**
 * List directory command.
 */
@Command(name = "ls", mixinStandardHelpOptions = true, description = "List directory.")
public class Ls extends SftpCommand implements Callable<Integer> {

	@Override
	public Integer call() throws Exception {
		SftpContainer container = getContainer();
		SftpFile[] files = container.getClient().ls(container.getCwd());
		for (int i = 0; i < files.length; i++) {
			container.getTerminal().writer().println(String.format("%10s %-30s %8d %15s",
					new Object[] { Util.getPermissionsString(files[i].getType(), files[i].getPermissions()),
							files[i].getName(), Long.valueOf(files[i].getSize()),
							DateFormat.getDateTimeInstance().format(new Date(files[i].getLastModified())) }));
		}
		return 0;
	}
}