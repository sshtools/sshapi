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

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;

/**
 * Implementations are responsible for managing host keys.
 */
public interface SshHostKeyManager {
	/**
	 * Get all valid host keys. Will be a list of {@link SshHostKey} objects.
	 * This list will consist of both permanent and temporary keys and is 
	 * guaranteed to be non-null.
	 * 
	 * @return host keys
	 */
	SshManagedHostKey[] getKeys();

	/**
	 * Remove a key.
	 * 
	 * @param hostKey key to remove
	 * @throws SshException  on error
	 */
	void remove(SshManagedHostKey hostKey) throws SshException;

	/**
	 * Get if this host key manager allows keys to be persisted.
	 * 
	 * @return writeable
	 * @see #add(SshHostKey, boolean)
	 */
	boolean isWriteable();

	/**
	 * Add a key, optionally persisting it to some kind of backing store.
	 * 
	 * @param hostKey key to add
	 * @param persist persist the key
	 * @throws SshException on any error adding the key
	 */
	void add(SshManagedHostKey hostKey, boolean persist) throws SshException;

	/**
	 * Get all the keys that are valid for the specified host and key type. The
	 * implementation may also use DNS to attempt to match against alternative
	 * hostnames (e.g. the IP address if the host is specified).
	 * <p>
	 * The returned list is guaranteed to be non-null, i.e. it will be empty if no
	 * matches are found.
	 * 
	 * @param host host to match
	 * @param type type (one of {@link SshConfiguration#PUBLIC_KEY_SSHDSS} or
	 *             {@link SshConfiguration#PUBLIC_KEY_SSHRSA},
	 *             {@link SshConfiguration#PUBLIC_KEY_ECDSA},
	 *             {@link SshConfiguration#PUBLIC_KEY_ED25519}.
	 * @return keys
	 */
	SshManagedHostKey[] getKeysForHost(String host, String type);
}
