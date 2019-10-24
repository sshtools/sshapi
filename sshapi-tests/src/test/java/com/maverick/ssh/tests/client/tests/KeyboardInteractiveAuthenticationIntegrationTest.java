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
