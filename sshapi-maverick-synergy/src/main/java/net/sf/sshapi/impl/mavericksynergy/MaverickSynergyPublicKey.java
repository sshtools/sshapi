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

import com.sshtools.common.publickey.SshPublicKeyFile;
import com.sshtools.common.ssh.SshKeyFingerprint;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshPublicKey;

/**
 * The Class MaverickSynergyPublicKey.
 */
public class MaverickSynergyPublicKey implements SshPublicKey {

	private byte[] key;
	private String fingerPrint;
	private Algorithm algorithm;
	private int bitLength;
	private com.sshtools.common.ssh.components.SshPublicKey publicKey;

	/**
	 * Instantiates a new maverick synergy public key.
	 *
	 * @param fingerprintHashingAlgorithm the fingerprint hashing algorithm
	 * @param publicKeyFile the public key file
	 * @throws SshException the ssh exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws com.sshtools.common.ssh.SshException error
	 */
	public MaverickSynergyPublicKey(String fingerprintHashingAlgorithm, SshPublicKeyFile publicKeyFile) throws com.sshtools.common.ssh.SshException, IOException {
		init(fingerprintHashingAlgorithm, publicKeyFile.toPublicKey());
	}

	/**
	 * Inits the.
	 *
	 * @param fingerprintHashingAlgorithm the fingerprint hashing algorithm
	 * @param publicKey the public key
	 * @throws SshException the ssh exception
	 */
	private void init(String fingerprintHashingAlgorithm, com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		this.publicKey = publicKey;
		key = publicKey.getEncoded();
		algorithm = Algorithm.fromAlgoName(publicKey.getAlgorithm());
		fingerPrint = MaverickSynergySshClient.stripAlgorithmFromFingerprint(SshKeyFingerprint.getFingerprint(key, fingerprintHashingAlgorithm));
		bitLength = publicKey.getBitLength();
	}

	/**
	 * Instantiates a new maverick synergy public key.
	 *
	 * @param fingerprintHashingAlgorithm the fingerprint hashing algorithm
	 * @param publicKey the public key
	 * @throws com.sshtools.common.ssh.SshException the ssh exception
	 */
	public MaverickSynergyPublicKey(String fingerprintHashingAlgorithm, com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		init(fingerprintHashingAlgorithm, publicKey);
	}

	/**
	 * Instantiates a new maverick synergy public key.
	 *
	 * @param fingerprintHashingAlgorithm the fingerprint hashing algorithm
	 * @param publicKey the public key
	 * @throws SshException the ssh exception
	 */
	public MaverickSynergyPublicKey(String fingerprintHashingAlgorithm, SshPublicKey publicKey) throws SshException {
		key = publicKey.getEncodedKey();
		try {
			fingerPrint = MaverickSynergySshClient.stripAlgorithmFromFingerprint(SshKeyFingerprint.getFingerprint(key, fingerprintHashingAlgorithm));
		} catch (com.sshtools.common.ssh.SshException e) {
			throw new SshException(SshException.GENERAL, e);
		}
		algorithm = publicKey.getAlgorithm();
		bitLength = publicKey.getBitLength();
	}

	/**
	 * Gets the public key.
	 *
	 * @return the public key
	 */
	public com.sshtools.common.ssh.components.SshPublicKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Gets the algorithm.
	 *
	 * @return the algorithm
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}

	/**
	 * Gets the fingerprint.
	 *
	 * @return the fingerprint
	 */
	public String getFingerprint() {
		return fingerPrint;
	}

	/**
	 * Gets the encoded key.
	 *
	 * @return the encoded key
	 */
	public byte[] getEncodedKey() {
		return key;
	}

	/**
	 * Gets the bit length.
	 *
	 * @return the bit length
	 */
	public int getBitLength() {
		return bitLength;
	}
}