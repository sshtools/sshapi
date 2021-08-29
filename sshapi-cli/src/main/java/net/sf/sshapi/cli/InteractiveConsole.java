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

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import net.sf.sshapi.cli.commands.Bye;
import net.sf.sshapi.cli.commands.Cd;
import net.sf.sshapi.cli.commands.Chgrp;
import net.sf.sshapi.cli.commands.Get;
import net.sf.sshapi.cli.commands.Help;
import net.sf.sshapi.cli.commands.Lcd;
import net.sf.sshapi.cli.commands.Lpwd;
import net.sf.sshapi.cli.commands.Ls;
import net.sf.sshapi.cli.commands.Mkdir;
import net.sf.sshapi.cli.commands.Put;
import net.sf.sshapi.cli.commands.Pwd;
import net.sf.sshapi.cli.commands.Rm;
import net.sf.sshapi.cli.commands.Rmdir;
import net.sf.sshapi.sftp.SftpClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;

@Command(name = "sftp-interactive", mixinStandardHelpOptions = false, description = "Interactive shell.", subcommands = {
		Ls.class, Cd.class, Pwd.class, Lcd.class, Lpwd.class, Mkdir.class, Rmdir.class, Rm.class,
		Get.class, Put.class, Bye.class, Chgrp.class, Help.class })
public class InteractiveConsole implements Runnable, SftpContainer {
	/**
	 * 
	 */
	private final sftp sftp;

	/**
	 * @param sftp
	 */
	public InteractiveConsole(sftp sftp) {
		this.sftp = sftp;
	}

	@Override
	public SftpClient getClient() {
		return this.sftp.getClient();
	}

	@Override
	public String getCwd() {
		return this.sftp.getCwd();
	}

	@Override
	public File getLcwd() {
		return this.sftp.getLcwd();
	}

	@Override
	public LineReader getLineReader() {
		return this.sftp.getLineReader();
	}

	@Override
	public Terminal getTerminal() {
		return this.sftp.getTerminal();
	}

	@Override
	public void run() {
		throw new ParameterException(this.sftp.getSpec().commandLine(), "Missing required subcommand");
	}

	@Override
	public void setCwd(String cwd) {
		this.sftp.setCwd(cwd);
	}

	@Override
	public void setLcwd(File lcwd) {
		this.sftp.setLcwd(lcwd);
	}

	@Override
	public CommandSpec getSpec() {
		return this.sftp.getSpec();
	}
}