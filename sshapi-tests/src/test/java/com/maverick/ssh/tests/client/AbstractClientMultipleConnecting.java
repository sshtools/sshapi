package com.maverick.ssh.tests.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import com.maverick.ssh.tests.AbstractSshTest;
import com.maverick.ssh.tests.ServerService.AuthenticationMethod;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import net.sf.sshapi.util.DumbHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;
import net.sf.sshapi.util.Util;

public abstract class AbstractClientMultipleConnecting extends AbstractSshTest {
	protected boolean administrator;
	private boolean threaded;
	protected List<SshClient> clients;

	public boolean isAdministrator() {
		return administrator;
	}

	public boolean isThreaded() {
		return threaded;
	}

	public abstract int getConnectionCount();

	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	@Before
	public final void onSetUp() throws Exception {
		Assume.assumeTrue(config.getName().startsWith("sshapi-client-"));
		onClientSetup();
		if (getUsername().equals("root")) {
			administrator = true;
		}
		connect();
	}
	
	protected void onClientSetup() {
		
	}

	protected String getUsername() {
		return config.getUsername();
	}

	@After
	public final void onTearDown() throws Exception {
		disconnect();
	}

	protected void deconfigureForMethods() throws Exception {
		disconnect();
		super.deconfigureForMethods();
		connect();
	}

	protected void configureForMethods(AuthenticationMethod... methods) throws Exception {
		disconnect();
		super.configureForMethods(methods);
		connect();
	}

	protected void disconnect() throws SshException, IOException {
		LOG.info("Disconnecting all client");
		if (clients != null) {
			for (SshClient ssh : clients) {
				if(ssh.isConnected()) {
					LOG.info("Disconnecting from {0}:{1}.", config.getServer(), config.getPort());
					ssh.close();
					LOG.info("Disconnected from {0}:{1}.", config.getServer(), config.getPort());
				}
			}
			clients.clear();
		}
	}

	protected void reconnect() throws Exception {
		if (clients != null && clients.size() > 0) {
			disconnect();
		}
		connect();
	}

	protected void connect() throws Exception {
		LOG.info("Connecting {0} clients.", getConnectionCount());
		SshConfiguration con = new SshConfiguration();
		con.setHostKeyValidator(new DumbHostKeyValidator());
		if (clients == null) {
			clients = new ArrayList<SshClient>();
		}
		for (int i = 0; i < getConnectionCount(); i++) {
			LOG.info("Connecting {0} to {1}:{2}.", i, config.getServer(), config.getPort());
			clients.add(con.open(getUsername(), config.getServer(), config.getPort()));
		}
		onConnectingSetUp();
	}

	protected void onConnectingSetUp() throws Exception {
	}

	protected SshPublicKeyAuthenticator createKey(String key, String fileName, String passphrase) throws IOException {
		String name = "/" + key + "/" + fileName;
		InputStream in = getClass().getResourceAsStream(name);
		if (in == null) {
			throw new IOException("Cannot find resource " + name);
		}
		try {
			return new DefaultPublicKeyAuthenticator(
					new SimplePasswordAuthenticator(passphrase == null ? null : passphrase.toCharArray()), Util.toByteArray(in));
		} finally {
			in.close();
		}
	}
}
