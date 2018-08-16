package net.sf.sshapi.impl.maverick16;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

public class MaverickSshPublicKey implements com.maverick.ssh.components.SshPublicKey {
	private final SshPublicKey key;

	public MaverickSshPublicKey(SshPublicKey key) {
		this.key = key;
	}

	public boolean verifySignature(byte[] signature, byte[] data) throws com.maverick.ssh.SshException {
		return false;
	}

	public void init(byte[] blob, int start, int len) throws com.maverick.ssh.SshException {
	}

	public String getFingerprint() throws com.maverick.ssh.SshException {
		try {
			return key.getFingerprint();
		} catch (SshException e) {
			throw new com.maverick.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	public byte[] getEncoded() throws com.maverick.ssh.SshException {
		try {
			return key.getEncodedKey();
		} catch (SshException e) {
			throw new com.maverick.ssh.SshException("Failed to get fingerprint", e);
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