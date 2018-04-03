package net.sf.sshapi;

import net.sf.sshapi.SshChannel.ChannelData;

public class DefaultChannelData implements ChannelData {
	int windowSize;
	int packetSize;
	long timeout;
	byte[] create;
	byte[] requestData;

	public DefaultChannelData(int windowSize, int packetSize, long timeout, byte[] requestData) {
		super();
		this.windowSize = windowSize;
		this.packetSize = packetSize;
		this.timeout = timeout;
		this.requestData = requestData;
	}

	public DefaultChannelData(int windowSize, int packetSize, long timeout, byte[] create, byte[] requestData) {
		super();
		this.windowSize = windowSize;
		this.packetSize = packetSize;
		this.timeout = timeout;
		this.create = create;
		this.requestData = requestData;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getPacketSize() {
		return packetSize;
	}

	public void setPacketSize(int packetSize) {
		this.packetSize = packetSize;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setCreate(byte[] create) {
		this.create = create;
	}

	public byte[] getRequestData() {
		return requestData;
	}

	public void setRequestData(byte[] requestData) {
		this.requestData = requestData;
	}

	public byte[] create() {
		return create;
	}

}
