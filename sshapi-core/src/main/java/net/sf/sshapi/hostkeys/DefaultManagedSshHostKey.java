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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Default implementation of a {@link SshManagedHostKey} that delegates mostly
 * to a {@link SshHostKey}, and allowing comments to be added to key for storage
 * by a {@link SshHostKeyManager}.
 *
 */
public class DefaultManagedSshHostKey implements SshManagedHostKey {

	private SshHostKey key;
	private String comments;


	/**
	 * Constructor, using the current user and hostname as the basis
	 * for the comment.
	 * 
	 * @param key      key
	 * @param comments comments
	 */
	public DefaultManagedSshHostKey(SshHostKey key) {
		this.key = key;
		try {
			try {
				this.comments = System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName();
			}
			catch(UnknownHostException uhe) {
				this.comments = System.getProperty("user.name") + "@localhost";
			}
		}
		catch(Exception e) {
			//
		}
	}

	/**
	 * Constructor
	 * 
	 * @param key      key
	 * @param comments comments
	 */
	public DefaultManagedSshHostKey(SshHostKey key, String comments) {
		this.key = key;
		this.comments = comments;
	}

	@Override
	public String getHost() {
		return key.getHost();
	}

	@Override
	public String getType() {
		return key.getType();
	}

	@Override
	public String getFingerprint() {
		return key.getFingerprint();
	}

	@Override
	public byte[] getKey() {
		return key.getKey();
	}

	@Override
	public String getComments() {
		return comments;
	}

}
