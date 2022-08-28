/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
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
