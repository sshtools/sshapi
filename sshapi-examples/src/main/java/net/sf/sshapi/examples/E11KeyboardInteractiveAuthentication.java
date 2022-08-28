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
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleKeyboardInteractiveAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E02ShellWithConsolePrompts}, except keyboard
 * interactive authentication is used instead of password.
 */
public class E11KeyboardInteractiveAuthentication {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());

		// Connect, authenticate
		try(SshClient client = config.open(Util.promptConnectionSpec(), new ConsoleKeyboardInteractiveAuthenticator())) {
			ExampleUtilities.dumpClientInfo(client);
			try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				ExampleUtilities.joinShellToConsole(shell);
			}
		} 
	}
}
