package net.sf.sshapi.impl.nassh.proto;

public class Packet {
	
	private int packetLength;
	private byte paddingLength;
	private byte[] payload;
	private byte[] randomPadding;
	private byte[] mac;

	public Packet() {
	}

	int getPacketLength() {
		return packetLength;
	}

	void setPacketLength(int packetLength) {
		this.packetLength = packetLength;
	}

	byte getPaddingLength() {
		return paddingLength;
	}

	void setPaddingLength(byte paddingLength) {
		this.paddingLength = paddingLength;
	}

	byte[] getPayload() {
		return payload;
	}

	void setPayload(byte[] payload) {
		this.payload = payload;
	}

	byte[] getRandomPadding() {
		return randomPadding;
	}

	void setRandomPadding(byte[] randomPadding) {
		this.randomPadding = randomPadding;
	}

	byte[] getMac() {
		return mac;
	}

	void setMac(byte[] mac) {
		this.mac = mac;
	}

}
