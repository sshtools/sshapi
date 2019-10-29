package net.sf.sshapi.impl.mavericksynergy;

import java.io.IOException;

import com.sshtools.common.publickey.SshPublicKeyFile;
import com.sshtools.common.ssh.SshKeyFingerprint;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshPublicKey;

public class MaverickSynergyPublicKey implements SshPublicKey {

	private byte[] key;
	private String fingerPrint;
	private Algorithm algorithm;
	private int bitLength;
	private com.sshtools.common.ssh.components.SshPublicKey publicKey;

	public MaverickSynergyPublicKey(String fingerprintHashingAlgorithm, SshPublicKeyFile publicKeyFile) throws com.sshtools.common.ssh.SshException, IOException {
		init(fingerprintHashingAlgorithm, publicKeyFile.toPublicKey());
	}

	private void init(String fingerprintHashingAlgorithm, com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		this.publicKey = publicKey;
		key = publicKey.getEncoded();
		algorithm = Algorithm.fromAlgoName(publicKey.getAlgorithm());
		fingerPrint = MaverickSynergySshClient.stripAlgorithmFromFingerprint(SshKeyFingerprint.getFingerprint(key, fingerprintHashingAlgorithm));
		bitLength = publicKey.getBitLength();
	}

	public MaverickSynergyPublicKey(String fingerprintHashingAlgorithm, com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		init(fingerprintHashingAlgorithm, publicKey);
	}

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

	public com.sshtools.common.ssh.components.SshPublicKey getPublicKey() {
		return publicKey;
	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	public String getFingerprint() {
		return fingerPrint;
	}

	public byte[] getEncodedKey() {
		return key;
	}

	public int getBitLength() {
		return bitLength;
	}
}