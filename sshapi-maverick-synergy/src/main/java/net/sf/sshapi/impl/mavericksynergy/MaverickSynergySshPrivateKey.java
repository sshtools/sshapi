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

import java.io.IOException;
import java.security.PrivateKey;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

/**
 * Maverick Synergy private key implementation
 */
public class MaverickSynergySshPrivateKey implements com.sshtools.common.ssh.components.SshPrivateKey {
	private final SshPrivateKey key;

	/**
	 * Constructor.
	 * 
	 * @param key
	 */
	public MaverickSynergySshPrivateKey(SshPrivateKey key) {
		this.key = key;
	}

	public byte[] sign(byte[] data) throws IOException {
		try {
			return key.sign(data);
		} catch (SshException e) {
			throw new IOException(
					String.format("Failed to sign %d bytes.", new Object[] { String.valueOf(data.length) }), e);
		}
	}

	public PrivateKey getJCEPrivateKey() {
		throw new UnsupportedOperationException();
	}

	public String getAlgorithm() {
		return key.getAlgorithm().toAlgoName();
	}

	@Override
	public byte[] sign(byte[] data, String signingAlgorithm) throws IOException {
		throw new UnsupportedOperationException();
	}
}