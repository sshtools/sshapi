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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;

public class KeyboardInteractiveAuthenticationIntegrationTest extends AbstractClientConnecting {
	final static String PASSPHRASE = "changeit";

	@Test(expected = SshException.class)
	public void testAuthenticateCancel() throws Exception {
		Assume.assumeTrue("Should support keyboard interactive.", ssh.getProvider().getCapabilities().contains(Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION));
		timeout(() -> {
			SshKeyboardInteractiveAuthenticator kbi = (name, instruction, prompt, echo) -> {
				return null;
			};
			ssh.authenticate(kbi);
			return null;
		}, 10000);
	}

	@Test
	public void testAuthenticate() throws Exception {
		Assume.assumeTrue("Should support keyboard interactive.", ssh.getProvider().getCapabilities().contains(Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION));
		timeout(() -> {
			SshKeyboardInteractiveAuthenticator kbi = (name, instruction, prompt, echo) -> {
				String[] results = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					assertEquals(config.getChallenge(i), prompt[i]);
					results[i] = config.getResponse(i);
				}
				return results;
			};
			boolean result = ssh.authenticate(kbi);
			assertTrue("Authentication must be complete.", result);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testAuthenticateFail() throws Exception {
		Assume.assumeTrue("Should support keyboard interactive.", ssh.getProvider().getCapabilities().contains(Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION));
		timeout(() -> {
			SshKeyboardInteractiveAuthenticator kbi = (name, instruction, prompt, echo) -> {
				String[] results = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					assertEquals(config.getChallenge(i), prompt[i]);
					results[i] = config.getResponse(i) + "XXXXXXXX";
				}
				return results;
			};
			boolean result = ssh.authenticate(kbi);
			assertFalse("Authentication must have failed.", result);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
}
