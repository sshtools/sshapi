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
package net.sf.sshapi.auth;

import java.io.File;

import net.sf.sshapi.SshClient;

/**
 * Authenticator used to key authentication.
 *
 */
public interface SshPublicKeyAuthenticator extends SshAuthenticator {

	@Override
	public default String getTypeName() {
		return "publickey";
	}
	
	/**
	 * Get the private key as a file. If the user provided a file, this will
	 * generally be the same reference. If the user provided a byte array, a
	 * temporary file will be created. It is up to the caller to delete the file
	 * as soon as possible, but failing that it will be deleted on JVM exit. The
	 * permission will only allow the user to read the file.
	 * 
	 * @return private key file
	 */
	File getPrivateKeyFile();

	/**
	 * Get the private key data. If the user provided a file, the byte array
	 * will be populated with the contents of the file, otherwise the byte
	 * array will be returned as is.
	 * 
	 * @return private key data
	 */
	byte[] getPrivateKey();

	/**
	 * Invoked when the session requires a passphrase (for a key).
	 * 
	 * @param session session
	 * @param message message
	 * @return passphrase or <code>null</code> if passphrase is not available
	 *         (e.g. cancelled)
	 */
	char[] promptForPassphrase(SshClient session, String message);
}
