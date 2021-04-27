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
package org.apache.maven.wagon.providers.ssh.sshapi;

import java.util.Arrays;

import org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo;

import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.DefaultManagedSshHostKey;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.Util;

/**
 * {@link SshHostKeyValidator} implementation that displays it's confirmations
 * of keys via Maven's {@link InteractiveUserInfo}. If none is supplied, the
 * prompt will be sent directly to the console.
 */
public class PromptingKeyValidator implements SshHostKeyValidator {

	private SshHostKeyManager mgr;
	private InteractiveUserInfo interactiveUserInfo;

	/**
	 * Constructor.
	 * 
	 * @param interactiveUserInfo interactive user info
	 * @param mgr SSHAPI key manager
	 */
	public PromptingKeyValidator(InteractiveUserInfo interactiveUserInfo, SshHostKeyManager mgr) {
		this.mgr = mgr;
		this.interactiveUserInfo = interactiveUserInfo;
	}

	public int verifyHost(SshHostKey hostKey) throws SshException {
		String type = hostKey.getType().substring(4).toUpperCase();
		if (mgr != null) {
			SshHostKey[] keys = mgr.getKeysForHost(hostKey.getHost(), hostKey.getType());
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
		boolean yes = interactiveUserInfo == null ? Util.promptYesNo(message + "\nAllow?") : interactiveUserInfo
			.promptYesNo(message + "\nAllow?");
		if (yes && mgr != null) {
			mgr.add(new DefaultManagedSshHostKey(hostKey), true);
			System.out.println("Warning: Permanently added '" + hostKey.getHost() + "' (" + type + ") to the list of known hosts.");
		}
		return yes ? SshHostKeyValidator.STATUS_HOST_KEY_VALID : SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
	}

}
