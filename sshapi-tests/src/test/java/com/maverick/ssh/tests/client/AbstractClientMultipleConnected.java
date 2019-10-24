package com.maverick.ssh.tests.client;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public abstract class AbstractClientMultipleConnected extends
		AbstractClientMultipleConnecting {

	protected void onConnectingSetUp() throws SshException, IOException {
		SimplePasswordAuthenticator pwd = new SimplePasswordAuthenticator(config.getPassword());
		for (SshClient ssh : clients) {
			boolean result = ssh.authenticate(pwd);
			assertTrue("Authentication must be successful.", result);
			assertTrue("Must be connected", ssh.isConnected());
		}
	}
}
