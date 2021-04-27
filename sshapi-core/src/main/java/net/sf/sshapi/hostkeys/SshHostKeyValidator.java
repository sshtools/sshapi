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
package net.sf.sshapi.hostkeys;

import net.sf.sshapi.SshException;

/**
 * Implementations are responsible for validating host keys during initial
 * connection.
 * 
 */
public interface SshHostKeyValidator {
	/**
	 * Host key is valid
	 * 
	 * @see #verifyHost(SshHostKey)
	 */
	final static int STATUS_HOST_KEY_VALID = 0;
	/**
	 * Host key is not known
	 * 
	 * @see #verifyHost(SshHostKey)
	 */
	final static int STATUS_HOST_KEY_UNKNOWN = 2;
	/**
	 * Host key is known, but is different. Note, some implementations may not
	 * make any distinction between {@link #STATUS_HOST_KEY_UNKNOWN} and
	 * {@link #STATUS_HOST_CHANGED}.
	 * 
	 * @see #verifyHost(SshHostKey)
	 */
	final static int STATUS_HOST_CHANGED = 3;

	/**
	 * Validate the provided host key and return one of
	 * {@link #STATUS_HOST_CHANGED}, {@link #STATUS_HOST_KEY_UNKNOWN} or
	 * {@link #STATUS_HOST_KEY_VALID}.
	 * 
	 * @param hostKey key
	 * @return status
	 * @throws SshException
	 */
	int verifyHost(SshHostKey hostKey) throws SshException;
}
