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

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;

/**
 * Extension of {@link SshPublicKeyAuthenticator} to be used for X509
 * keys. Provider must support {@link Capability#X509_PUBLIC_KEY}.
 *
 */
public interface SshX509PublicKeyAuthenticator extends SshPublicKeyAuthenticator {
	
	/**
	 * Get the alias in the keystore that represents the key.
	 * 
	 * @return alias
	 */
	String getAlias();
	
	/**
	 * Invoked when the session requires the key passphrase (for a key). By
	 * default this will be the same as the {@link SshPublicKeyAuthenticator#promptForPassphrase(SshClient, String)}
	 * method.
	 * 
	 * @param session session
	 * @param message message
	 * @return key passphrase or <code>null</code> if passphrase is not available
	 *         (e.g. cancelled)
	 */
	default char[] promptForKeyPassphrase(SshClient session, String message) {
		return promptForPassphrase(session, message);
	}
}
