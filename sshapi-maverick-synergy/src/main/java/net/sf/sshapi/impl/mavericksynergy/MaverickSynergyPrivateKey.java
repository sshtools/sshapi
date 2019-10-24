package net.sf.sshapi.impl.mavericksynergy;

import java.io.IOException;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

class MaverickSynergyPrivateKey implements SshPrivateKey {

	com.sshtools.common.ssh.components.SshPrivateKey privateKey;

	public MaverickSynergyPrivateKey(com.sshtools.common.ssh.components.SshPrivateKey privateKey) {
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
	public Algorithm getAlgorithm() {
		return Algorithm.fromAlgoName(privateKey.getAlgorithm());
	}
}