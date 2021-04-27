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
import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

/**
 * This example demonstrates the simplest use of the API, opening up a
 * connection, creating a shell, and joining the channel streams to standard
 * input / output to create a simple remote shell application.
 */
public final class E01Shell {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Set the basic configuration for this conneciton
		// Put your own detains in here
		String username = "CHANGE_THIS";
		String hostname = "CHANGE_THIS";
		char[] password = "CHANGE_THIS".toCharArray();
		int port = 22;

		// Connect and authenticate
		try(SshClient client = Ssh.open(username, hostname, port, new SimplePasswordAuthenticator(password))) {

			/*
			 * Look at the source of ExampleUtilities to see how to query a provider
			 * for its capabilities
			 */
			ExampleUtilities.dumpClientInfo(client);
			
			// Create a shell on the server and join it to the console
			try(SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				/*
				 * Call the utility method to join the remote streams to the
				 * console streams
				 */
				ExampleUtilities.joinShellToConsole(shell);
			} 
		} 
	}
}
