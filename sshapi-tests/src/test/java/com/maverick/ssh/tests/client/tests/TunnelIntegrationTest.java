package com.maverick.ssh.tests.client.tests;

import static com.maverick.ssh.tests.Size.size;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.maverick.echo.EchoClient;
import com.maverick.echo.EchoServer;
import com.maverick.ssh.tests.Util;
import com.maverick.ssh.tests.client.AbstractClientConnected;
import com.maverick.ssh.tests.client.util.PortForwardEventCapture;

import net.sf.sshapi.Capability;
import net.sf.sshapi.forwarding.SshPortForward;

public class TunnelIntegrationTest extends AbstractClientConnected {
	private EchoServer echoServer;

	@Before
	public final void onConnectedSetUp() throws Exception {
		echoServer = new EchoServer(0);
		echoServer.start();
		LOG.info("Listening on {0}:{1} ", echoServer.getListeningAddress(), echoServer.getListeningPort());
	}

	@After
	public final void onConnectedTearDown() throws Exception {
		if (echoServer != null) {
			echoServer.stop();
		}
	}

	@Test
	public void testLocalForwardingKnownPort() throws Exception {
		Assume.assumeTrue("Must support local forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.LOCAL_PORT_FORWARD));
		timeout(() -> {
			int echoServerPort = echoServer.getListeningPort();
			String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
			int port = Util.findRandomPort();
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			try (SshPortForward fwd = ssh.localForward("0.0.0.0", port, echoServerAddress, echoServerPort)) {
				int boundPort = fwd.getBoundPort();
				Assert.assertEquals("There must be active components.", 1, ssh.getAllActiveComponents().size());
				Assert.assertEquals("Bound port must be same as known port", port, boundPort);
				EchoClient ec = new EchoClient("127.0.0.1", boundPort, 10, 100, 1000, size().kib(1).toBytesInt(),
						size().kib(64).toBytesInt());
				ec.run(10000);
				assertEquals("There must be no errors", 0, ec.getErrors());
			}
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			return null;
		}, 120000);
	}

	@Test
	public void testLocalForwardingEvents() throws Exception {
		Assume.assumeTrue("Must support local forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.LOCAL_PORT_FORWARD));
		Assume.assumeTrue("Must support channel data events",
				ssh.getProvider().getCapabilities().contains(Capability.CHANNEL_DATA_EVENTS));
		timeout(() -> {
			int echoServerPort = echoServer.getListeningPort();
			String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
			int port = Util.findRandomPort();
			SshPortForward fwd = ssh.createLocalForward("0.0.0.0", port, echoServerAddress, echoServerPort);
			PortForwardEventCapture cap = new PortForwardEventCapture();
			fwd.addListener(cap);
			ssh.addPortForwardListener(cap);
			fwd.open();
			EchoClient ec;
			try {
				ec = new EchoClient("127.0.0.1", fwd.getBoundPort(), 10, 100, 1000, size().kib(1).toBytesInt(),
						size().kib(64).toBytesInt());
				ec.run(10000);
			} finally {
				fwd.close();
			}

			// TODO maverick can fire data events after closing. Wait for a bit for them to
			// all complete
			Thread.sleep(10000);

			cap.assertEvents(1, 1, 1, 10, 10, ec.getBytesTransfered(), ec.getBytesTransfered(), 0);
			return null;
		}, 120000);
	}

	@Test
	public void testLocalForwardingRandomPort() throws Exception {
		Assume.assumeTrue("Must support local forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.LOCAL_PORT_FORWARD));
		timeout(() -> {
			int echoServerPort = echoServer.getListeningPort();
			String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			try (SshPortForward fwd = ssh.localForward("0.0.0.0", 0, echoServerAddress, echoServerPort)) {
				int boundPort = fwd.getBoundPort();
				Assert.assertEquals("There must be active components.", 1, ssh.getAllActiveComponents().size());
				Assume.assumeTrue(
						"Bound port must be greater than zero. Suggests provider does not support returning actual bound port when zero is used for local port.",
						boundPort > 0);
//				EchoClient ec = new EchoClient("127.0.0.1", boundPort, 10, 100, 1000, size().kib(1).toBytesInt(),
//						size().kib(64).toBytesInt());
				EchoClient ec = new EchoClient("127.0.0.1", boundPort, 1, 1, 1, size().kib(1).toBytesInt(),
						size().kib(64).toBytesInt());
				ec.run(10000);
				assertEquals("There must be no errors", 0, ec.getErrors());
			}
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			return null;
		}, 120000);
	}

	@Test
	public void testRemoteForwardingKnownPort() throws Exception {
		Assume.assumeTrue("Must support remote forwarding",
				ssh.getProvider().getCapabilities().contains(Capability.REMOTE_PORT_FORWARD));
		timeout(() -> {
			int echoServerPort = echoServer.getListeningPort();
			String echoServerAddress = echoServer.getListeningAddress().getHostAddress();
			int port = Util.findRandomPort();
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			try (SshPortForward fwd = ssh.remoteForward("127.0.0.1", port, echoServerAddress, echoServerPort)) {
				Assert.assertEquals("There must be active components.", 1, ssh.getAllActiveComponents().size());
				int boundPort = fwd.getBoundPort();
				Assert.assertEquals("Bound port must be zero", 0, boundPort);
				EchoClient ec = new EchoClient("127.0.0.1", port, 10, 100, 1000, size().kib(1).toBytesInt(),
						size().kib(64).toBytesInt());
				ec.run(10000);
				assertEquals("There must be no errors", 0, ec.getErrors());
			}
			Assert.assertEquals("There must be no active components.", 0, ssh.getAllActiveComponents().size());
			return null;
		}, 120000);
	}
}
