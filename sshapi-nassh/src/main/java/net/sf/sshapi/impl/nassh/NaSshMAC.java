package net.sf.sshapi.impl.nassh;

public interface NaSshMAC extends NaSshComponent {
	int getBlockSize();

	void init(byte[] key) throws Exception;

	void update(int i);

	void update(byte foo[], int s, int l);

	void doFinal(byte[] buf, int offset);
}
