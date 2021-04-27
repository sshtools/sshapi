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
package com.maverick.ssh.tests.client;

import static org.junit.Assert.assertTrue;

import org.junit.Before;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public abstract class AbstractClientConnected extends AbstractClientConnecting {

	@Before
	public final void onConnectingSetUp() throws Exception {
		timeout(() -> {
			LOG.info("Authenticating to {0}@{1}:{2}", ssh.getUsername(), config.getServer(),
					config.getPort());
			boolean result = ssh.authenticate(createAuthenticator());
			LOG.info("Authenticated with {0}@{1}:{2}", ssh.getUsername(), config.getServer(),
					config.getPort());
			assertTrue("Authentication must be successful.", result);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 30000);
	}

	protected SshAuthenticator createAuthenticator() {
		return new SimplePasswordAuthenticator(config.getPassword());
	}
}
