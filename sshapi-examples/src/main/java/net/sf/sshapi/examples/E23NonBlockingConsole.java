package net.sf.sshapi.examples;
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E02ShellWithConsolePrompts}, except this 
 * example uses SSHAPI's non-blocking methods.
 * <p>
 * While this is more complex to implement, using non-blocking I/O is likely to more efficient
 * at scale (assuming the underlying provider is not using SSHAPI's own simulated non-blocking
 * methods).
 */
public class E23NonBlockingConsole {
	static int dataTransferredIn = 0;
	static int dataTransferredOut = 0;

	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		
		/* Start connection */
		Future<SshClient> connectFuture = config.openLater(Util.promptConnectionSpec());
		
		/* Blocks until client is available. This can be on any thread (in fact it should
		 * be on another thread, or this technique isn't much different to the blocking API!).
		 * For the purpose of the example we use the same thread though.
		 *
		 * Because we passed in an authenticator 
		 */
		SshClient client = connectFuture.get();
		ExampleUtilities.dumpClientInfo(client);
		System.out.println("Remote identification: " + client.getRemoteIdentification());
		
		/* Now authenticate. We COULD have passed in authenticators to the SshConfiguration.openLater()
		 * method above, in which case the Future returned would have waited until authentication was
		 * done as well (XXX TODO XXX), but let's do this separately to demonstrate how you 
		 * can authenticate separately.
		 */
		Future<Boolean> authenticationFuture = client.authenticateLater(new ConsolePasswordAuthenticator());
		if(!authenticationFuture.get()) {
			throw new Exception("Failed to authenticate.");
		}
		
		/* Now get the shell. Because we want to set input handlers, we don't use client.shellLater(),
		 * instead we create the shell, set the handlers then open it */
		SshShell shell = client.createShell("dumb", 80, 24, 0,0 ,null);
		
		/* Set the handlers that are invoked when data arrives on shell in or shell err **/
		shell.setInput((buffer) -> Util.write(buffer, System.out));
		shell.setErrInput((buffer) -> Util.write(buffer, System.err));
		
		/* Now open the shell to actually start it and get the streams moving */
		Future<Void> shellFuture = shell.openLater();
		
		/* Wait for shell to be ready */
		shellFuture.get();
		
		/* Read from console and write to shell out. We have to translate between
		 * console I/O streams and NIO buffer */
		byte[] buf = new byte[1024];
		int r;
		while( ( r = System.in.read(buf)) != -1)
			shell.writeLater(ByteBuffer.wrap(buf, 0, r));
		
		
		/* Wait for 1 minute to close (demonstrating timeout) */
		Future<Void> closeFuture = client.closeLater();
		closeFuture.get(1, TimeUnit.MINUTES);
		
	}
}
