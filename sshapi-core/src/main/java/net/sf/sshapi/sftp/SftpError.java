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
package net.sf.sshapi.sftp;

/**
 * Unchecked exception.
 */
@SuppressWarnings("serial")
public class SftpError extends RuntimeException {
	/**
	 * Constructor.
	 * 
	 * @param message message
	 */
	public SftpError(String message) {
		this("Unchecked exception.", new SftpException(SftpException.GENERAL, message));
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause   cause
	 */
	public SftpError(SftpException cause) {
		this("Unchecked exception.", cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message message
	 * @param cause   cause
	 */
	public SftpError(String message, Exception cause) {
		super(message, cause instanceof SftpException ? cause : new SftpException(SftpException.GENERAL, cause));
	}

	/**
	 * Get the underlying {@link SftpException}.
	 * 
	 * @return causing SFTP exception
	 */
	public SftpException getSftpCause() {
		return (SftpException) getCause();
	}

}
