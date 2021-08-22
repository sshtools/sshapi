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
package net.sf.sshapi.identity;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.SshPublicKey;

/**
 * Some server / provider combinations will be able to support management of
 * authorized keys. Implementations of this interface are returned by
 * {@link SshClient#createPublicKeySubsystem()}, which may then be used the
 * keys.
 * 
 */
public interface SshPublicKeySubsystem
		extends SshLifecycleComponent<SshLifecycleListener<SshPublicKeySubsystem>> {

	/**
	 * Add a new authorized key.
	 * 
	 * @param key
	 *            key
	 * @param comment
	 *            comment
	 * @throws SshException
	 */
	void add(SshPublicKey key, String comment) throws SshException;

	/**
	 * List all of the available authorized keys.
	 * 
	 * @return list of all of the available authorized keys.
	 * @throws SshException
	 */
	SshPublicKey[] list() throws SshException;

	/**
	 * Remove a key from the list of available authorized keys. Any clients using
	 * this key will no longer be able to authenticate as this user.
	 * 
	 * @param key
	 * @throws SshException
	 */
	void remove(SshPublicKey key) throws SshException;
}
