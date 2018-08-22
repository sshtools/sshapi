package net.sf.sshapi.impl.maverickng;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

public class MaverickNGSshPublicKey implements com.sshtools.common.ssh.components.SshPublicKey {
	private final SshPublicKey key;

	public MaverickNGSshPublicKey(SshPublicKey key) {
		this.key = key;
	}

	public boolean verifySignature(byte[] signature, byte[] data) throws com.sshtools.common.ssh.SshException {
		return false;
	}

	public void init(byte[] blob, int start, int len) throws com.sshtools.common.ssh.SshException {
	}

	public String getFingerprint() throws com.sshtools.common.ssh.SshException {
		try {
			return key.getFingerprint();
		} catch (SshException e) {
			throw new com.sshtools.common.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	public byte[] getEncoded() throws com.sshtools.common.ssh.SshException {
		try {
			return key.getEncodedKey();
		} catch (SshException e) {
			throw new com.sshtools.common.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	public int getBitLength() {
		return key.getBitLength();
	}

	public String getAlgorithm() {
		return key.getAlgorithm();
	}

	public String getSigningAlgorithm() {
		return key.getAlgorithm();
	}

	public String test() {
		throw new UnsupportedOperationException();
	}
}