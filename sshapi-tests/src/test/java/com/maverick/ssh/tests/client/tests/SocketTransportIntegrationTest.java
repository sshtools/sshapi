package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.maverick.ssh.tests.AbstractSshTest;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.util.DumbHostKeyValidator;

public class SocketTransportIntegrationTest extends AbstractSshTest {
	@Test
	public void testConnect() throws Exception {
		timeout(() -> {
			SshConfiguration con = new SshConfiguration();
			con.setHostKeyValidator(new DumbHostKeyValidator());
			SshClient ssh = con.open(config.getUsername(), config.getServer(), config.getPort());
			assertTrue("Must be connected", ssh.isConnected());
			ssh.close();
			assertFalse("Must be disconnected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test(expected = SshException.class)
	public void testConnectBadHost() throws Exception {
		timeout(() -> {
			SshConfiguration con = new SshConfiguration();
			con.setHostKeyValidator(new DumbHostKeyValidator());
			SshClient ssh = con.open(config.getUsername(), "localhost", 65534);
			ssh.close();
			return null;
		}, 10000);
	}
}
