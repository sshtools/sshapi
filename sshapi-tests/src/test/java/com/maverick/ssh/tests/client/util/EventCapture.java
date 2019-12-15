package com.maverick.ssh.tests.client.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshLifecycleListener;

public class EventCapture<C extends SshLifecycleComponent<?, C>> implements SshLifecycleListener<C> {
	AtomicInteger opens = new AtomicInteger();
	AtomicInteger closings = new AtomicInteger();
	AtomicInteger closes = new AtomicInteger();

	@Override
	public void opened(C channel) {
		opens.incrementAndGet();
	}

	@Override
	public void closing(C channel) {
		closings.incrementAndGet();
	}

	@Override
	public void closed(C channel) {
		closes.incrementAndGet();
	}

	public void assertEvents(int open, int closing, int closed) {
		Assert.assertEquals("Open events", open, opens.get());
		Assert.assertEquals("Closing events", closing, closings.get());
		Assert.assertEquals("Closed events", closed, closes.get());
	}

	@Override
	public String toString() {
		return "EventCapture [opens=" + opens + ", closings=" + closings + ", closes=" + closes + "]";
	}
}