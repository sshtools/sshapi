package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnecting;

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
