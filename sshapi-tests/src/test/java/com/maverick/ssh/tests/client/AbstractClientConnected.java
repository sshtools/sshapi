package com.maverick.ssh.tests.client;

import static org.junit.Assert.assertTrue;

import org.junit.Before;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public abstract class AbstractClientConnected extends AbstractClientConnecting {
	@Before
	public final void onConnectingSetUp() throws Exception {
		timeout(() -> {
			SshConfiguration.getLogger().log(Level.INFO, "Authenticating to " + ssh.getUsername() + "@" + config.getServer() + ":" + config.getPort());
			boolean result = ssh.authenticate(createAuthenticator());
			SshConfiguration.getLogger().log(Level.INFO, "Authenticated with " + ssh.getUsername() + "@" + config.getServer() + ":" + config.getPort());
			assertTrue("Authentication must be successful.", result);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 30000);
	}

	protected SshAuthenticator createAuthenticator() {
		return new SimplePasswordAuthenticator(config.getPassword());
	}
}
