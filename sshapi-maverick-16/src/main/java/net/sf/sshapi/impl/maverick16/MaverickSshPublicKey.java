package net.sf.sshapi.impl.maverick16;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

public class MaverickSshPublicKey implements com.maverick.ssh.components.SshPublicKey {
	private final SshPublicKey key;

	public MaverickSshPublicKey(SshPublicKey key) {
		this.key = key;
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] data) throws com.maverick.ssh.SshException {
		return false;
	}

	@Override
	public void init(byte[] blob, int start, int len) throws com.maverick.ssh.SshException {
	}

	@Override
	public String getFingerprint() throws com.maverick.ssh.SshException {
		try {
			return key.getFingerprint();
		} catch (SshException e) {
			throw new com.maverick.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	@Override
	public byte[] getEncoded() throws com.maverick.ssh.SshException {
		try {
			return key.getEncodedKey();
		} catch (SshException e) {
			throw new com.maverick.ssh.SshException("Failed to get fingerprint", e);
		}
	}

	@Override
	public int getBitLength() {
		return key.getBitLength();
	}

	@Override
	public String getAlgorithm() {
		return key.getAlgorithm();
	}

	@Override
	public String getSigningAlgorithm() {
		return key.getAlgorithm();
	}

	@Override
	public String test() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEncodingAlgorithm() {
		throw new UnsupportedOperationException();
	}
}