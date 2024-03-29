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
package net.sf.sshapi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extension of a {@link SshStreamChannel} that adds the Extended Input Stream,
 * used for STDERR.
 * 
 */

public interface SshCommand
		extends SshExtendedChannel<SshStreamChannelListener<SshCommand>, SshCommand> {
	
	/**
	 * Return value when exit code is not (yet) known.
	 */
	public final static int EXIT_CODE_NOT_RECEIVED = -1; 
	
	/**
	 * Get the extended input stream.
	 * 
	 * @return extended input stream
	 * @throws IOException
	 */
	InputStream getExtendedInputStream() throws IOException;

	/**
	 * Get the exit code of this command or shell when known, otherwise {@link #EXIT_CODE_NOT_RECEIVED}.
	 * 
	 * @return exit code
	 * @throws IOException
	 */
	int exitCode() throws IOException;
}