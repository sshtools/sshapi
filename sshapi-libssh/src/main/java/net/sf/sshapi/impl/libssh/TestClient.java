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
package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleLogger;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Test client.
 */
public class TestClient {
	static {
		((ConsoleLogger) SshConfiguration.getLogger()).setDefaultLevel(Level.DEBUG);
	}

	/**
	 * Entry point.
	 * 
	 * @param args arguments
	 * @throws IOException on error
	 */
	public static void main(String[] args) throws IOException {

		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());

		// Create the client using that configuration, then connect and authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			try (SshPortForward local = client.localForward(null, 19999, "localhost", 9999)) {
				try (Socket s = new Socket("localhost", 19999)) {
					OutputStream out = s.getOutputStream();
					InputStream in = s.getInputStream();
					out.write("HELLO_CLIENT________".getBytes());
					out.flush();
					byte[] b = new byte[20];
					int r = in.read(b);
					if (r != -1) {
						System.out.println("Got: " + r + " : " + new String(b, 0, r));
						r = in.read();
						if (r != -1) {
							System.out.println("Unexpected byte!");
						}
					}

				}
			}
		}

	}
}
