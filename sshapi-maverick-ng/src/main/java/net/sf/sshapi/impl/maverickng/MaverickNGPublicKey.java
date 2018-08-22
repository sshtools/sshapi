package net.sf.sshapi.impl.maverickng;

import java.io.IOException;

import com.sshtools.common.publickey.SshPublicKeyFile;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;

public class MaverickNGPublicKey implements SshPublicKey {

	private byte[] key;
	private String fingerPrint;
	private String algorithm;
	private int bitLength;
	private com.sshtools.common.ssh.components.SshPublicKey publicKey;

	public MaverickNGPublicKey(SshPublicKeyFile publicKeyFile) throws com.sshtools.common.ssh.SshException, IOException {
		init(publicKeyFile.toPublicKey());
	}

	private void init(com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		this.publicKey = publicKey;
		key = publicKey.getEncoded();
		algorithm = publicKey.getAlgorithm();
		fingerPrint = publicKey.getFingerprint();
		bitLength = publicKey.getBitLength();
	}

	public MaverickNGPublicKey(com.sshtools.common.ssh.components.SshPublicKey publicKey)
			throws com.sshtools.common.ssh.SshException {
		init(publicKey);
	}

	public MaverickNGPublicKey(SshPublicKey publicKey) throws SshException {
		key = publicKey.getEncodedKey();
		fingerPrint = publicKey.getFingerprint();
		algorithm = publicKey.getAlgorithm();
		bitLength = publicKey.getBitLength();
	}

	public com.sshtools.common.ssh.components.SshPublicKey getPublicKey() {
		return publicKey;
	}

	public String getAlgorithm() {
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