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

	public void channelOpened(int type, SshPortForwardTunnel channel) {
		channel.addDataListener(this);
		channelOpens.incrementAndGet();
	}

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