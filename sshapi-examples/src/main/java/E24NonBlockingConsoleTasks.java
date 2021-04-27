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
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E23NonBlockingConsole}, but instead
 * {@link Ssh#then(java.util.concurrent.Future, net.sf.sshapi.Ssh.SshCallable)}
 * is used to demonstrate a sequence of chained tasks.
 */
public class E24NonBlockingConsoleTasks {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		Ssh.then(config.openLater(Util.promptConnectionSpec()), (client) -> {
			ExampleUtilities.dumpClientInfo(client);
			System.out.println("Remote identification: " + client.getRemoteIdentification());
			Ssh.then(client.authenticateLater(new ConsolePasswordAuthenticator()), (authenticated) -> {
				if (!authenticated) {
					throw new SshException(SshException.AUTHENTICATION_FAILED);
				}
				/*
				 * Now get the shell. Because we want to set input handlers, we
				 * don't use client.shellLater(), instead we create the shell,
				 * set the handlers then open it
				 */
				SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
				/*
				 * Set the handlers that are invoked when data arrives on shell
				 * in or shell err
				 **/
				shell.setInput((buffer) -> Util.write(buffer, System.out));
				shell.setErrInput((buffer) -> Util.write(buffer, System.err));
				Ssh.then(shell.openLater(), (v) -> {
					/*
					 * Read from console and write to shell out. We have to
					 * translate between console I/O streams and NIO buffer
					 */
					byte[] buf = new byte[1024];
					int r;
					while ((r = System.in.read(buf)) != -1)
						shell.writeLater(ByteBuffer.wrap(buf, 0, r));
				});
			});
		}).closeLater().get(1, TimeUnit.MINUTES);
	}
}
