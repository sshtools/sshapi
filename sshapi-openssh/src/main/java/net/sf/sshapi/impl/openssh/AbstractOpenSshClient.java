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
package net.sf.sshapi.impl.openssh;

import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.util.Util;

/**
 * Abstract OpenSsh Client.
 */
public interface AbstractOpenSshClient {

	/**
	 * Read two streams (one in a separate thread) and dump them to {@link System#err} and {@link System#out}.
	 * 
	 * @param in in
	 * @param errIn error in
	 * @throws IOException on error
	 */
	default void pump(InputStream in, InputStream errIn) throws IOException {
		Thread readOutput = new Thread("ReadSftpStdoout") {
			@Override
			public void run() {
				try {
					Util.joinStreams(errIn, System.err);
				} catch (Exception ioe) {
				}
			}
		};
		readOutput.start();
		Util.joinStreams(in, System.out);
	}
}
