package com.maverick.ssh.tests.client.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;

import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshDataProducingComponent;

public class DataEventCapture<C extends SshDataProducingComponent<?, C>> extends EventCapture<C> implements SshChannelListener<C>, SshDataListener<C> {
	public AtomicInteger eofs = new AtomicInteger();
	public AtomicLong dataIn = new AtomicLong();
	public AtomicLong dataOut = new AtomicLong();
	public AtomicLong dataErr = new AtomicLong();
	
	@Override
	public void eof(C channel) {
		eofs.incrementAndGet();
	}
	
	@Override
	public void data(C channel, int direction, byte[] buf, int off, int len) {
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

	public void assertEvents(int open, int closing, int closed, int eof, long in, long out, long err) {
		assertEvents(open, closing, closed);
		Assert.assertEquals("EOF events", eof, eofs.get());
		Assert.assertEquals("Bytes in", in, dataIn.get());
		Assert.assertEquals("Bytes out", out, dataOut.get());
		Assert.assertEquals("Bytes err", err, dataErr.get());
	}
}