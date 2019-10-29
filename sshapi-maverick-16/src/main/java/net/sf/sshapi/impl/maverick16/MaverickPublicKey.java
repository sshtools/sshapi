package net.sf.sshapi.impl.maverick16;

import java.io.IOException;

import com.maverick.ssh.SshKeyFingerprint;
import com.sshtools.publickey.SshPublicKeyFile;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshPublicKey;

public class MaverickPublicKey implements SshPublicKey {

	private Algorithm algorithm;
	private int bitLength;
	private String fingerPrint;
	private byte[] key;
	private com.maverick.ssh.components.SshPublicKey publicKey;

	public MaverickPublicKey(String hashingAlgorithm, com.maverick.ssh.components.SshPublicKey publicKey) throws com.maverick.ssh.SshException {
		init(hashingAlgorithm, publicKey);
	}

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