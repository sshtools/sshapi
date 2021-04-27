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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.cli.commands.Cd;
import net.sf.sshapi.cli.commands.Get;
import net.sf.sshapi.cli.commands.Lcd;
import net.sf.sshapi.cli.commands.Lpwd;
import net.sf.sshapi.cli.commands.Ls;
import net.sf.sshapi.cli.commands.Mkdir;
import net.sf.sshapi.cli.commands.Put;
import net.sf.sshapi.cli.commands.Pwd;
import net.sf.sshapi.cli.commands.Rm;
import net.sf.sshapi.cli.commands.Rmdir;
import net.sf.sshapi.sftp.SftpClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * Java clone of the de-facto standard OpenSSH sftp command.
 */
@Command(name = "sftp", mixinStandardHelpOptions = true, description = "Secure file transfer.")
public class sftp extends AbstractSshCommand implements Logger, Callable<Integer>, SftpContainer {

	@Command(name = "sftp-interactive", mixinStandardHelpOptions = true, description = "Interactive shell.", subcommands = {
			Ls.class, Cd.class, Pwd.class, Lcd.class, Lpwd.class, Mkdir.class, Rmdir.class, Rm.class,
			Get.class, Put.class })
	class InteractiveConsole implements Runnable, SftpContainer {
		@Override
		public SftpClient getClient() {
			return sftp.this.sftp;
		}

		@Override
		public String getCwd() {
			return sftp.this.cwd;
		}

		@Override
		public File getLcwd() {
			return lcwd;
		}

		@Override
		public LineReader getLineReader() {
			return sftp.this.getLineReader();
		}

		@Override
		public Terminal getTerminal() {
			return sftp.this.getTerminal();
		}

		@Override
		public void run() {
			throw new ParameterException(spec.commandLine(), "Missing required subcommand");
		}

		@Override
		public void setCwd(String cwd) {
			sftp.this.cwd = cwd;
		}

		@Override
		public void setLcwd(File lcwd) {
			sftp.this.setLcwd(lcwd);
		}
	}

	/**
	 * Entry point.
	 * 
	 * @param args command line arguments
	 * @throws SshException
	 */
	public static void main(String[] args) throws Exception {
		sftp client = new sftp();
		System.exit(new CommandLine(client).execute(args));
	}

	/**
	 * Parse a space separated string into a list, treating portions quotes with
	 * single quotes as a single element. Single quotes themselves and spaces can be
	 * escaped with a backslash.
	 * 
	 * @param command command to parse
	 * @return parsed command
	 */
	public static List<String> parseQuotedString(String command) {
		List<String> args = new ArrayList<String>();
		boolean escaped = false;
		boolean quoted = false;
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < command.length(); i++) {
			char c = command.charAt(i);
			if (c == '"' && !escaped) {
				if (quoted) {
					quoted = false;
				} else {
					quoted = true;
				}
			} else if (c == '\\' && !escaped) {
				escaped = true;
			} else if (c == ' ' && !escaped && !quoted) {
				if (word.length() > 0) {
					args.add(word.toString());
					word.setLength(0);
					;
				}
			} else {
				word.append(c);
			}
		}
		if (word.length() > 0)
			args.add(word.toString());
		return args;
	}

	@Option(names = { "-P", "--port" }, description = "Port number on which the server is listening.")
	private int port = 22;

	@Parameters(index = "0", description = "Destination.")
	private String destination;

	@Spec
	private CommandSpec spec;

	private SftpClient sftp;
	private String cwd;
	private boolean exitWhenDone;
	private File lcwd = new File(System.getProperty("user.dir"));

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public sftp() throws SshException {
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
			}
			return 1;
		} catch (Exception e) {
			System.err.println("ssh: " + e.getMessage());
			return 1;
		}
		return 0;
	}

	@Override
	public SftpClient getClient() {
		return sftp;
	}

	@Override
	public String getCwd() {
		return cwd;
	}

	@Override
	public File getLcwd() {
		return lcwd;
	}

	@Override
	public LineReader getLineReader() {
		return reader;
	}

	@Override
	public Terminal getTerminal() {
		return terminal;
	}

	@Override
	public void setCwd(String cwd) {
		this.cwd = cwd;
	}

	@Override
	public void setLcwd(File lcwd) {
		this.lcwd = lcwd;
	}

	@Override
	protected int getPort() {
		return port;
	}

	@Override
	protected boolean isBatchMode() {
		return false;
	}

	protected void onStart() throws SshException, IOException {

		SshClient client = connect(destination);
		sftp = client.sftp();
		cwd = sftp.getDefaultPath();
		try {
			PrintWriter err = terminal.writer();
			err.println(String.format("Connected to %s", client.getHostname()));
			do {
				try {
					String cmd = reader.readLine("sftp> ");
					if (cmd != null && cmd.length() > 0) {
						List<String> newargs = parseQuotedString(cmd);
						newargs.removeIf(item -> item == null || "".equals(item));
						String[] args = newargs.toArray(new String[0]);
						if (args.length > 0) {
							CommandLine cl = new CommandLine(new InteractiveConsole());
							cl.setTrimQuotes(true);
							cl.setUnmatchedArgumentsAllowed(true);
							cl.setUnmatchedOptionsAllowedAsOptionParameters(true);
							cl.setUnmatchedOptionsArePositionalParams(true);
							cl.execute(args);
						}
					}

				} catch (Exception e) {
					err.println(String.format("%s", e.getMessage()));
				}
			} while (!exitWhenDone);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new IllegalStateException("Failed to open console.", e1);
		} finally {
			sftp.close();
		}

	}
}
