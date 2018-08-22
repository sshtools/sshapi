package net.sf.sshapi.impl.maverickng;

import java.io.IOException;
import java.security.PrivateKey;

import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;

public class MaverickNGSshPrivateKey implements com.sshtools.common.ssh.components.SshPrivateKey {
	private final SshPrivateKey key;

	public MaverickNGSshPrivateKey(SshPrivateKey key) {
		this.key = key;
	}

	public byte[] sign(byte[] data) throws IOException {
		try {
			return key.sign(data);
		} catch (SshException e) {
			throw new IOException(
					String.format("Failed to sign %d bytes.", new Object[] { String.valueOf(data.length) }), e);
		}
	}

	public PrivateKey getJCEPrivateKey() {
		throw new UnsupportedOperationException();
	}

	public String getAlgorithm() {
		return key.getAlgorithm();
	}
}