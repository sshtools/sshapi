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

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Java clone of the de-facto standard OpenSSH ssh command.
 */
@Command(name = "ssh", mixinStandardHelpOptions = true, description = "Secure shell.")
public class ssh extends AbstractSshCommand implements Logger, Callable<Integer> {

	@Option(names = { "-p", "--port" }, description = "Port number on which the server is listening.")
	private int port = 22;

    @Parameters(index = "0", description = "Destination.")
	private String destination;

    @Parameters(index = "1", arity = "0..1", description = "Command to run.")
	private String command;

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public ssh() throws SshException {
	}

	protected void onStart() throws SshException, IOException {
		String term = System.getenv("TERM");
		if(term == null || term.equals("")) {
			term = "dumb";
		}
		// TODO sizes, resizing
		try(SshClient client = connect(destination)) {
			if(command == null) {
				try(SshShell shell = client.shell(term, 80, 24, 0, 0, null)) {
					joinChannelToConsole(shell);
				} 
			}
			else {
				try(SshCommand cmd = client.command(command, term, 80, 24, 0, 0, null)) {
					joinChannelToConsole(cmd);
				}
			}
		}
	}

		
	/**
	 * Entry point.
	 * 
	 * @param args command line arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		ssh client = new ssh();
		System.exit(new CommandLine(client).execute(args));
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
	protected boolean isBatchMode() {
		return false;
	}

	@Override
	protected int getPort() {
		return port;
	}
	
	static void joinChannelToConsole(final SshExtendedChannel<?, ?> channel) throws IOException, SshException {
		AtomicBoolean fin = new AtomicBoolean();
		AtomicBoolean closed = new AtomicBoolean();
		Thread mainThread = Thread.currentThread();
		Thread readErrThread = new Thread() {
			public void run() {
				try {
					Util.joinStreams(channel.getExtendedInputStream(), channel.getOutputStream());
				} catch (Exception e) {
				}
			}
		};
		readErrThread.start();
		Thread readInThread = new Thread() {
			public void run() {
				/*
				 * Wrapping in a channel allows this thread to be interrupted
				 * (on Linux at least, other OS's .. YMMV
				 */
				try (InputStream in = Channels.newInputStream((new FileInputStream(FileDescriptor.in)).getChannel())) {
					Util.joinStreams(in, channel.getOutputStream());
					channel.getInputStream().close();
				} catch (Exception e) {
				}
				if (!closed.get()) {
					closed.set(true);
					try {
						channel.close();
					} catch (IOException e) {
					}
					if (!fin.get())
						mainThread.interrupt();
				}
			}
		};
		readInThread.start();
		Util.joinStreams(channel.getInputStream(), System.out);
		fin.set(true);
		readInThread.interrupt();
		readErrThread.interrupt();
		if (!closed.get()) {
			closed.set(true);
			try {
				channel.close();
			} catch (IOException e) {
			}
		}
	}
}
