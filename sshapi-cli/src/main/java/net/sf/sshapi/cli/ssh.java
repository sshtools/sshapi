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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jline.utils.Signals;

import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedChannel;
import net.sf.sshapi.SshExtendedChannel.Signal;
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

	@Option(names = { "-L",
			"--local-forward" }, description = "Specifies that connections to the given TCP port on the local (client) host are to be forwarded to the given host andport, on the remote side.")
	private String[] localForwards;

	@Parameters(index = "0", description = "Destination.")
	private String destination;

	@Parameters(index = "1", arity = "0..1", description = "Command to run.")
	private String command;

	private int height;
	private int width;
	private ScheduledExecutorService terminalDimensionsMonitor;

	/**
	 * Constructor.
	 * 
	 * @throws SshException
	 */
	public ssh() throws SshException {
	}

	protected boolean calcWidthAndHeight() {
		int nwidth = 0;
		int nheight = 0;
		if (terminal == null) {
			nwidth = 80;
			nheight = 24;
		} else {
			nwidth = terminal.getWidth();
			nheight = terminal.getHeight();
		}
		try {
			return width != -1 && (width != nwidth || height != nheight);
		} finally {
			width = nwidth;
			height = nheight;
		}
	}

	protected void onStart() throws SshException, IOException {
		String term = System.getenv("TERM");
		if (term == null || term.equals("")) {
			term = "dumb";
		}
		calcWidthAndHeight();
		try (SshClient client = connect(destination)) {
			if (localForwards != null) {
				for (String localForward : localForwards) {
					String[] spec = localForward.split(":");
					if (spec.length == 4) {
						client.localForward(spec[0], Integer.parseInt(spec[1]), spec[2], Integer.parseInt(spec[3]));
					} else if (spec.length == 3) {
						client.localForward(null, Integer.parseInt(spec[0]), spec[1], Integer.parseInt(spec[2]));
					} else {
						throw new IllegalArgumentException("Invalid local forwarding specification.");
					}
				}
			}
			if (command == null) {
				try (SshShell shell = client.shell(term, width, height, 0, 0, null)) {
					if (terminal != null) {
						terminalDimensionsMonitor = Executors.newSingleThreadScheduledExecutor();
						terminalDimensionsMonitor.scheduleAtFixedRate(() -> {
							if (calcWidthAndHeight()) {
								try {
									shell.requestPseudoTerminalChange(width, height, 0, 0);
								} catch (SshException e) {
									debug("Failed to change terminal dimensions.");
								}
							}
						}, 500, 500, TimeUnit.MILLISECONDS);
						terminal.echo(false);
					}
					Runnable ctrlC = () -> {
						try {
							if (provider.getCapabilities().contains(Capability.SIGNALS)) {
								shell.sendSignal(Signal.INT);
							} else {
								/* Will echo Ctrl+C twice on linux, maybe others */
								shell.getOutputStream().write(3);
							}
						} catch (IOException e) {
							if (!isQuiet())
								error("Failed to send signal. {0}", e.getMessage());
						}
					};
					Signals.register("INT", ctrlC);
					try {
						joinChannelToConsole(shell);
					} finally {
						Signals.unregister("INT", ctrlC);
						System.out.println();
					}
				} finally {
					if(terminalDimensionsMonitor != null)
						terminalDimensionsMonitor.shutdown();
				}
			} else {
				try (SshCommand cmd = client.command(command, term, 80, 24, 0, 0, null)) {
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
				 * Wrapping in a channel allows this thread to be interrupted (on Linux at
				 * least, other OS's .. YMMV
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
		try {
			Util.joinStreams(channel.getInputStream(), System.out);
		} finally {
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
}
