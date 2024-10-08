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
package com.maverick.ssh.tests.client.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;

import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;

public class PortForwardEventCapture extends EventCapture<SshPortForward> implements SshPortForwardListener, SshDataListener<SshPortForwardTunnel> {
	
	AtomicInteger channelOpens = new AtomicInteger();	
	AtomicInteger channelCloses = new AtomicInteger();
	AtomicLong dataIn = new AtomicLong();
	AtomicLong dataOut = new AtomicLong();
	AtomicLong dataErr = new AtomicLong();

	@Override
	public void channelOpened(int type, SshPortForwardTunnel channel) {
		channel.addDataListener(this);
		channelOpens.incrementAndGet();
	}

	@Override
	public void channelClosed(int type, SshPortForwardTunnel channel) {
		channel.removeDataListener(this);
		channelCloses.incrementAndGet();
	}
	
	@Override
	public void data(SshPortForwardTunnel channel, int direction, byte[] buf, int off, int len) {
		switch(direction) {
		case SshDataListener.RECEIVED:
			dataIn.addAndGet(len);
			break;
		case SshDataListener.SENT:
			dataOut.addAndGet(len);
			break;
		case SshDataListener.EXTENDED:
			dataErr.addAndGet(len);
			break;
		}
	}

	public void assertEvents(int open, int closing, int closed, int opens, int closes, long in, long out, long err) {
		assertEvents(open, closing, closed);
		Assert.assertEquals("Channel open events", opens, channelOpens.get());
		Assert.assertEquals("Close close events", closes, channelCloses.get());
		Assert.assertEquals("Bytes in", in, dataIn.get());
		Assert.assertEquals("Bytes out", out, dataOut.get());
		Assert.assertEquals("Bytes err", err, dataErr.get());
	}
}