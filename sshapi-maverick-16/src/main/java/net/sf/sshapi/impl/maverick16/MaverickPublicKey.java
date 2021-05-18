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
package net.sf.sshapi.impl.maverick16;

import java.io.IOException;

import com.maverick.ssh.SshKeyFingerprint;
import com.sshtools.publickey.SshPublicKeyFile;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshPublicKey;

/**
 * Maverick implementation of a Public Key
 */
public class MaverickPublicKey implements SshPublicKey {

	private Algorithm algorithm;
	private int bitLength;
	private String fingerPrint;
	private byte[] key;
	private com.maverick.ssh.components.SshPublicKey publicKey;

	/**
	 * Constructor.
	 * 
	 * @param hashingAlgorithm hashing algorithm
	 * @param publicKey public key
	 * @throws com.maverick.ssh.SshException on error
	 * @throws IOException on error
	 */
	public MaverickPublicKey(String hashingAlgorithm, com.maverick.ssh.components.SshPublicKey publicKey) throws com.maverick.ssh.SshException {
		init(hashingAlgorithm, publicKey);
	}

	/**
	 * Constructor.
	 * 
	 * @param hashingAlgorithm hashing algorithm
	 * @param publicKey public key
	 * @throws SshException on error
	 * @throws IOException on error
	 */
	public MaverickPublicKey(String hashingAlgorithm, SshPublicKey publicKey) throws SshException {
		key = publicKey.getEncodedKey();
		try {
			fingerPrint = MaverickSshClient.stripAlgorithmFromFingerprint(SshKeyFingerprint.getFingerprint(publicKey.getEncodedKey(), hashingAlgorithm));
		} catch (com.maverick.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
		fingerPrint = publicKey.getFingerprint();
		algorithm = publicKey.getAlgorithm();
		bitLength = publicKey.getBitLength();
	}

	/**
	 * Constructor.
	 * 
	 * @param hashingAlgorithm hashing algorithm
	 * @param publicKeyFile public key file
	 * @throws com.maverick.ssh.SshException on error
	 * @throws IOException on error
	 */
	public MaverickPublicKey(String hashingAlgorithm, SshPublicKeyFile publicKeyFile) throws com.maverick.ssh.SshException,
			IOException {
		init(hashingAlgorithm, publicKeyFile.toPublicKey());
	}

	@Override
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	@Override
	public int getBitLength() {
		return bitLength;
	}

	@Override
	public byte[] getEncodedKey() {
		return key;
	}

	@Override
	public String getFingerprint() {
		return fingerPrint;
	}

	/**
	 * Get the native public key object
	 * 
	 * @return native public key object
	 */
	public com.maverick.ssh.components.SshPublicKey getPublicKey() {
		return publicKey;
	}

	private void init(String hashingAlgorithm, com.maverick.ssh.components.SshPublicKey publicKey) throws com.maverick.ssh.SshException {
		this.publicKey = publicKey;
		key = publicKey.getEncoded();
		algorithm = Algorithm.fromAlgoName(publicKey.getAlgorithm());
		fingerPrint = MaverickSshClient.stripAlgorithmFromFingerprint(SshKeyFingerprint.getFingerprint(key, hashingAlgorithm));
		bitLength = publicKey.getBitLength();
	}
}