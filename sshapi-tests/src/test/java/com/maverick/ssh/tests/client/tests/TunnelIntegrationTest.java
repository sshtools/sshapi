package com.maverick.ssh.tests.client.tests;

import static com.maverick.ssh.tests.Size.size;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.maverick.echo.EchoClient;
import com.maverick.echo.EchoServer;
import com.maverick.ssh.tests.Util;
import com.maverick.ssh.tests.client.AbstractClientConnected;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshException;
import net.sf.sshapi.forwarding.SshPortForward;

public class TunnelIntegrationTest extends AbstractClientConnected {
	private EchoServer echoServer;

	@Override
	public boolean isThreaded() {
		// Remote forwarding won't work without it
		return true;
	}

	@Before
	public final void onConnectedSetUp() throws Exception {
		echoServer = new EchoServer(0);
		echoServer.start();
		System.out.println("Listening on  " + echoServer.getListeningAddress() + ":" + echoServer.getListeningPort());
	}

	@After
	public final void onConnectedTearDown() throws Exception {
		if (echoServer != null) {
			echoServer.stop();
		}
	}

	@Test
	public void testLocalForwardingKnownPort() throws SshException, IOException {
		Assume.assumeTrue("Must support local forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.LOCAL_PORT_FORWARD));
		int echoServerPort = echoServer.getListeningPort();
		String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
		int port = Util.findRandomPort();
		Assert.assertEquals("Must be a not channels open.", 0, ssh.getChannelCount());
		try (SshPortForward fwd = ssh.localForward("0.0.0.0", port, echoServerAddress, echoServerPort)) {
			int boundPort = fwd.getBoundPort();
			Assert.assertEquals("Must be a channel open.", 1, ssh.getChannelCount());
			Assert.assertEquals("Bound port must be same as known port", port, boundPort);
			EchoClient ec = new EchoClient("127.0.0.1", boundPort, 10, 100, 1000, size().kib(1).toBytesInt(),
					size().kib(64).toBytesInt());
			ec.run(10000);
			assertEquals("There must be no errors", 0, ec.getErrors());
		}
		Assert.assertEquals("There must be no channels.", 0, ssh.getChannelCount());
	}

	@Test
	public void testLocalForwardingRandomPort() throws SshException, IOException {
		Assume.assumeTrue("Must support local forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.LOCAL_PORT_FORWARD));
		int echoServerPort = echoServer.getListeningPort();
		String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
		Assert.assertEquals("Must be a not channels open.", 0, ssh.getChannelCount());
		Assert.assertEquals("There must be no channels.", 0, ssh.getChannelCount());
		try (SshPortForward fwd = ssh.localForward("0.0.0.0", 0, echoServerAddress, echoServerPort)) {
			int boundPort = fwd.getBoundPort();
			Assert.assertEquals("Must be a channel open.", 1, ssh.getChannelCount());
			Assume.assumeTrue(
					"Bound port must be greater than zero. Suggests provider does not support returning actual bound port when zero is used for local port.",
					boundPort > 0);
			EchoClient ec = new EchoClient("127.0.0.1", boundPort, 10, 100, 1000, size().kib(1).toBytesInt(),
					size().kib(64).toBytesInt());
			ec.run(10000);
			assertEquals("There must be no errors", 0, ec.getErrors());
		}
		Assert.assertEquals("There must be no channels.", 0, ssh.getChannelCount());
	}

	@Test
	public void testRemoteForwardingKnownPort() throws SshException, IOException {
		Assume.assumeTrue("Must support remote forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.REMOTE_PORT_FORWARD));
		int echoServerPort = echoServer.getListeningPort();
		String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
		int port = Util.findRandomPort();
		Assert.assertEquals("There must be no channels.", 0, ssh.getChannelCount());
		try (SshPortForward fwd = ssh.remoteForward("127.0.0.1", port, echoServerAddress, echoServerPort)) {
			Assert.assertEquals("Must be a channel open.", 1, ssh.getChannelCount());
			int boundPort = fwd.getBoundPort();
			Assert.assertEquals("Bound port must be zero", 0, boundPort);
			EchoClient ec = new EchoClient("127.0.0.1", boundPort, 10, 100, 1000, size().kib(1).toBytesInt(),
					size().kib(64).toBytesInt());
			ec.run(10000);
			assertEquals("There must be no errors", 0, ec.getErrors());
		}
		Assert.assertEquals("There must be no channels.", 0, ssh.getChannelCount());
	}
}
