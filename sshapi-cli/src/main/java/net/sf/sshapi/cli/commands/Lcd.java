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
import java.util.concurrent.Callable;

import net.sf.sshapi.cli.SftpContainer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "lcd", mixinStandardHelpOptions = true, description = "Change local directory.")
public class Lcd extends SftpCommand implements Callable<Integer> {

	@Parameters(index = "0", arity = "0..1", description = "Directory to change to.")
	private File directory;

	public Lcd() {
	}

	@Override
	public Integer call() throws Exception {
		SftpContainer container = getContainer();
		if (!directory.isAbsolute())
			directory = new File(container.getLcwd(), directory.getPath());
		if (directory.isDirectory())
			container.setLcwd(directory.getCanonicalFile());
		else
			container.getTerminal().writer().println(String.format("%s is not a directory.", directory));
		return 0;
	}
}