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
