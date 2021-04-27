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
package com.maverick.ssh.tests;

import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshConfiguration;

public abstract class AbstractServer implements ServerService {
	private static final Logger LOG = SshConfiguration.getLogger();
	protected List<AuthenticationMethod> methods = new ArrayList<AuthenticationMethod>();
	private boolean started;

	public void addRequiredAuthentication(AuthenticationMethod method) {
		methods.add(method);
	}

	public void removeRequiredAuthentication(AuthenticationMethod method) {
		methods.remove(method);
	}

	public final void start() throws Exception {
		LOG.info("Starting {0}", getClass().getSimpleName());
		doStart();
		started = true;
		LOG.info("Started {0}", getClass().getSimpleName());
	}

	protected abstract void doStart() throws Exception;

	public final void stop() throws Exception {
		LOG.info("Stopping {0}", getClass().getSimpleName());
		try {
			doStop();
		} finally {
			started = false;
			LOG.info("Stopped {0}", getClass().getSimpleName());
		}
	}

	protected boolean isStarted() {
		return started;
	}

	protected abstract void doStop() throws Exception;

	public void restart() throws Exception {
		if (started) {
			stop();
		}
		start();
	}
}
