package com.maverick.ssh.tests.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import com.maverick.ssh.tests.AbstractSshTest;
import com.maverick.ssh.tests.ServerService.AuthenticationMethod;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import net.sf.sshapi.util.DumbHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;
import net.sf.sshapi.util.Util;

public abstract class AbstractClientConnecting extends AbstractSshTest {
	protected boolean administrator;
	private boolean threaded;

	public boolean isAdministrator() {
		return administrator;
	}

	public boolean isThreaded() {
		return threaded;
	}

	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	protected SshClient ssh;

	@Before
	public final void onSetUp() throws Exception {
		Assume.assumeTrue(config.getName().startsWith("sshapi-client-"));
		if (getUsername().equals("root")) {
			administrator = true;
		}
		connect();
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

	protected void timedOut(Callable<Void> runnable, long timeout, Thread tthread, AtomicBoolean dn) throws InterruptedException {
		if (ssh != null && ssh.isConnected()) {
			SshConfiguration.getLogger().log(Level.ERROR, "Trying disconnecting the client.");
			ssh.closeQuietly();
			Thread.sleep(timeout);
			SshConfiguration.getLogger().log(Level.ERROR, "Disconnecting client had no effect, trying an interupt");
		}
		super.timedOut(runnable, timeout, tthread, dn);
	}

	protected void disconnect() throws SshException, IOException {
		if (ssh != null) {
			SshConfiguration.getLogger().log(Level.INFO, "Disconnecting from " + config.getServer() + ":" + config.getPort());
			if (ssh.isConnected())
				ssh.close();
			SshConfiguration.getLogger().log(Level.INFO, "Disconnected from " + config.getServer() + ":" + config.getPort());
		}
	}

	protected void reconnect() throws SshException, IOException {
		if (ssh.isConnected()) {
			disconnect();
		}
		connect();
	}

	protected void connect() throws SshException, IOException {
		SshConfiguration sshconfig = new SshConfiguration();
		sshconfig.setFingerprintHashingAlgorithm(config.getFingerprintHashingAlgorithm());
		sshconfig.setHostKeyValidator(new DumbHostKeyValidator());
		SshConfiguration.getLogger().log(Level.INFO, "Connecting to " + config.getServer() + ":" + config.getPort());
		ssh = sshconfig.createClient();
		long startedConnecting = System.currentTimeMillis();
		ssh.connect(getUsername(), config.getServer(), config.getPort());
		SshConfiguration.getLogger().log(Level.INFO, "Connected to " + config.getServer() + ":" + config.getPort() + ". Took " + (System.currentTimeMillis() - startedConnecting));
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
