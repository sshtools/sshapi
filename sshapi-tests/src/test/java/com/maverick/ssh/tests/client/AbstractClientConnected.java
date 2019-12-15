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
