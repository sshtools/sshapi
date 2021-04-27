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
package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public class PasswordAuthenticationIntegrationTest extends AbstractClientConnecting {
	@Test
	public void testAuthenticate() throws Exception {
		timeout(() -> {
			assertTrue("Authentication must be complete.", ssh.authenticate(new SimplePasswordAuthenticator(config.getPassword())));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
	
	@Test(expected = SshException.class)
	public void testAuthenticateCancel() throws Exception {
		timeout(() -> {
			ssh.authenticate(new SshPasswordAuthenticator() {
				@Override
				public char[] promptForPassword(SshClient session, String message) {
					return null;
				}
			});
			return null;
		}, 10000);
	}
	
	@Test(expected = SshException.class)
	public void testAuthenticateTimeout() throws Exception {
		Assume.assumeTrue("Must support data timeouts",ssh.getProvider().getCapabilities().contains(Capability.IO_TIMEOUTS));
		AtomicBoolean flag = new AtomicBoolean();
		timeout(() -> {
			ssh.authenticate(new SshPasswordAuthenticator() {
				@Override
				public char[] promptForPassword(SshClient session, String message) {
					try {
						LOG.info("Waiting for 3 minutes (should be enough for server to timeout");
						Thread.sleep(180000);
						LOG.info("Waited for 3 minutes");
						flag.set(true);
					} catch (InterruptedException e) {
						LOG.info("Interrupted!");
					}
					return null;
				}
			});
			return null;
		}, 300000);
		Assert.assertFalse("Mustn't have finished sleep while prompting.", flag.get());
		LOG.info("Done");
	}

	@Test
	public void testAuthenticateFail() throws Exception {
		timeout(() -> {
			SimplePasswordAuthenticator pwd = new SimplePasswordAuthenticator(
					(new String(config.getPassword()) + "XXXXXXXXXXxx").toCharArray());
			boolean result = ssh.authenticate(pwd);
			assertFalse("Authentication must have failed.", result);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
}
