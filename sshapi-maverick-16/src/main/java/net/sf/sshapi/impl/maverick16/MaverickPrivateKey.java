package net.sf.sshapi.impl.maverick16;

import java.io.IOException;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

class MaverickPrivateKey implements SshPrivateKey {

	com.maverick.ssh.components.SshPrivateKey privateKey;

	public MaverickPrivateKey(com.maverick.ssh.components.SshPrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	@Override
	public byte[] sign(byte[] data) throws SshException {
		try {
			return privateKey.sign(data);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public String getAlgorithm() {
		return privateKey.getAlgorithm();
	}
}