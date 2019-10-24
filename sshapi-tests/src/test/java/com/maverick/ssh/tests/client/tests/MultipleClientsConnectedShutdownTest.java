package com.maverick.ssh.tests.client.tests;

import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientMultipleConnected;

public class MultipleClientsConnectedShutdownTest extends AbstractClientMultipleConnected {

	int connectionCount = 5;
	
	@Test
	public void test100ClientsConnectedShutdown() throws Exception {
		server.stop();
	}

	@Override
	public int getConnectionCount() {
		return connectionCount;
	}
}
