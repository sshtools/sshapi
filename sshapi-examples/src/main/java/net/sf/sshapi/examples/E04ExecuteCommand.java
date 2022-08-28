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
import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates executing a command instead of a shell.
 */
public class E04ExecuteCommand {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		
		// Create the client using that configuration and connect
		try (SshClient client = Ssh.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			
			// Execute the command and read back its output
			try (SshCommand command = client.command("ls /etc")) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(command.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null)
					System.out.println(line);
			}
		}
	}
}
