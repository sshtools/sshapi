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
package net.sf.sshapi.impl.mavericksynergy;

import java.security.PublicKey;

import com.sshtools.common.ssh.SecurityLevel;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

/**
 * Maverick synergy public key implementation.
 *
 */
public class MaverickSynergySshPublicKey implements com.sshtools.common.ssh.components.SshPublicKey {
	private final SshPublicKey key;

	/**
	 * Constructor.
	 * 
	 * @param key key
	 */
	public MaverickSynergySshPublicKey(SshPublicKey key) {
		this.key = key;
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] data) throws com.sshtools.common.ssh.SshException {
		return false;
	}

	@Override
	public com.sshtools.common.ssh.components.SshPublicKey init(byte[] blob, int start, int len) throws com.sshtools.common.ssh.SshException {
		key.ini
		return this;
	}

	@Override
	public String getFingerprint() throws com.sshtools.common.ssh.SshException {
		try {
			return key.getFingerprint();
		} catch (SshException e) {
			throw new com.sshtools.common.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	@Override
	public byte[] getEncoded() throws com.sshtools.common.ssh.SshException {
		try {
			return key.getEncodedKey();
		} catch (SshException e) {
			throw new com.sshtools.common.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	@Override
	public int getBitLength() {
		return key.getBitLength();
	}

	@Override
	public String getAlgorithm() {
		return key.getAlgorithm().toAlgoName();
	}

	@Override
	public String getSigningAlgorithm() {
		return key.getAlgorithm().toAlgoName();
	}

	@Override
	public String test() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEncodingAlgorithm() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SecurityLevel getSecurityLevel() {
		return SecurityLevel.WEAK;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public PublicKey getJCEPublicKey() {
		throw new UnsupportedOperationException();
	}
}