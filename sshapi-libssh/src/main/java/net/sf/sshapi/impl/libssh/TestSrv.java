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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Test server.
 */
public class TestSrv {

	/**
	 * Entry point.
	 * 
	 * @param args arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		try (ServerSocket ss = new ServerSocket(9999)) {
			while (true) {
				try (Socket s = ss.accept()) {
					InputStream in = s.getInputStream();
					byte[] b = new byte[20];
					int r = in.read(b);
					if (r != -1) {
						System.out.println("Got: " + r + " : " + new String(b, 0, r) + " from " + s.getPort());
						OutputStream out = s.getOutputStream();
						out.write("HELLO_SERVER________".getBytes());
						out.flush();
					} else
						System.out.println("Broke early");
				}
			}
		}
	}
}
