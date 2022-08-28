package net.sf.sshapi.impl.nassh;

public interface NaSshPublicKey extends NaSshComponent {
	void init() throws Exception;

	void update(byte[] buffer) throws Exception;

	boolean verify(byte[] signature) throws Exception;

	byte[] sign() throws Exception;
}
