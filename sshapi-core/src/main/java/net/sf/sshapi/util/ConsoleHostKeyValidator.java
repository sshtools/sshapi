/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.util;

import java.util.Arrays;

import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;

/**
 * A simple host key validator that prompts to accept or reject on the console.
 * If a {@link SshHostKeyManager} is provided, then the keys can be persisted
 * and checked against the a store of known keys.
 */

public class ConsoleHostKeyValidator implements SshHostKeyValidator {

	private SshHostKeyManager keyManager;

	/**
	 * Constructor without host key manager. No keys will be persisted.
	 */
	public ConsoleHostKeyValidator() {
		this(null);
	}

	/**
	 * Constructor using a host key manager. If no manager is provided, keys
	 * will not be persisted.
	 * 
	 * @param keyManager host key manager.
	 */
	public ConsoleHostKeyValidator(SshHostKeyManager keyManager) {
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
		String message = "The authenticity of host '" + hostKey.getHost() + "' can't be established.\n";
		String keyAlgorithm = hostKey.getType();
		String hexFingerprint = hostKey.getFingerprint();
		message += (keyAlgorithm == null ? "?" : keyAlgorithm) + " key fingerprint is "
			+ (hexFingerprint == null ? "unknown" : hexFingerprint);
		boolean yes = Util.promptYesNo(message + "\nAllow?");
		if (yes && keyManager != null) {
			keyManager.add(hostKey, true);
			System.out.println("Warning: Permanently added '" + hostKey.getHost() + "' (" + type + ") to the list of known hosts.");
		}
		return yes ? SshHostKeyValidator.STATUS_HOST_KEY_VALID : SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
	}
}