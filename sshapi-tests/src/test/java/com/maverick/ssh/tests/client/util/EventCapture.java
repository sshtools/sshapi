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