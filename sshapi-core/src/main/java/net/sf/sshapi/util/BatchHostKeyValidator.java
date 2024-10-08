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
package net.sf.sshapi.util;

import java.util.Arrays;

import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;

/**
 * A simple host key validator that will only allow if a key is already in the
 * provided host key manager. For example, can be used to simulate "batch mode"
 * for the SCP command.
 */

public class BatchHostKeyValidator implements SshHostKeyValidator {

	private SshHostKeyManager keyManager;

	/**
	 * Constructor.
	 * 
	 * @param keyManager key manager
	 */
	public BatchHostKeyValidator(SshHostKeyManager keyManager) {
		if (keyManager == null) {
			throw new IllegalArgumentException(
				"A host key manager must be provided for this key validator. Does the current provider support host key management?");
		}
		this.keyManager = keyManager;
	}

	public int verifyHost(SshHostKey hostKey) throws SshException {
		String type = hostKey.getType().substring(4).toUpperCase();
		if (keyManager != null) {
			SshHostKey[] keys = keyManager.getKeysForHost(hostKey.getHost(), hostKey.getType());
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					if (Arrays.equals(keys[i].getKey(), hostKey.getKey())) {
						return SshHostKeyValidator.STATUS_HOST_KEY_VALID;
					}
				}
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @");
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println("IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!");
				System.out.println("Someone could be eavesdropping on you right now (man-in-the-middle attack)!");
				System.out.println("It is also possible that the " + type + " host key has just been changed.");
				System.out.println("The fingerprint for the " + type + " key sent by the remote host is");
				System.out.println(hostKey.getFingerprint() + ".");
				System.out.println("Please contact your system administrator.");
				System.out.println("Add correct host key to your known hosts database");
				return SshHostKeyValidator.STATUS_HOST_CHANGED;
			}
		}
		return SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
	}
}