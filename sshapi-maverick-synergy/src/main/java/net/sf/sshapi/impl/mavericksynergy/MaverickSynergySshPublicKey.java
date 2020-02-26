package net.sf.sshapi.impl.mavericksynergy;

import java.security.Key;

import com.sshtools.common.ssh.SecurityLevel;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

public class MaverickSynergySshPublicKey implements com.sshtools.common.ssh.components.SshPublicKey {
	private final SshPublicKey key;

	public MaverickSynergySshPublicKey(SshPublicKey key) {
		this.key = key;
	}

	@Override
	public boolean verifySignature(byte[] signature, byte[] data) throws com.sshtools.common.ssh.SshException {
		return false;
	}

	@Override
	public void init(byte[] blob, int start, int len) throws com.sshtools.common.ssh.SshException {
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
		return SecurityLevel.NONE;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public Key getJCEPublicKey() {
		throw new UnsupportedOperationException();
	}
}