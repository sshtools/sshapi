package net.sf.sshapi.impl.maverickng;

import java.io.IOException;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

class MaverickNGPrivateKey implements SshPrivateKey {

	com.sshtools.common.ssh.components.SshPrivateKey privateKey;

	public MaverickNGPrivateKey(com.sshtools.common.ssh.components.SshPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public byte[] sign(byte[] data) throws SshException {
		try {
			return privateKey.sign(data);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public String getAlgorithm() {
		return privateKey.getAlgorithm();
	}
}