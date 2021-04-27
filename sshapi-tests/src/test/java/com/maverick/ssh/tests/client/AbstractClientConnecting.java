/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
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

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
import net.sf.sshapi.util.DumbHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;
import net.sf.sshapi.util.Util;

public abstract class AbstractClientConnecting extends AbstractSshTest {
	protected boolean administrator;

	public boolean isAdministrator() {
		return administrator;
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
			LOG.warn("Trying disconnecting the client.");
			ssh.closeQuietly();
			Thread.sleep(timeout);
			LOG.warn("Disconnecting client had no effect, trying an interupt");
		}
		super.timedOut(runnable, timeout, tthread, dn);
	}

	protected void disconnect() throws SshException, IOException {
		if (ssh != null) {
			LOG.info("Disconnecting from {0}:{1}", config.getServer(), config.getPort());
			if (ssh.isConnected())
				ssh.close();
			LOG.info("Disconnected from {0}:{1}", config.getServer(), config.getPort());
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
		LOG.info("Connecting to {0}:{1} as {2}",config.getServer(), config.getPort(), getUsername());
		ssh = sshconfig.createClient();
		long startedConnecting = System.currentTimeMillis();
		if(ssh.getProvider().getCapabilities().contains(Capability.IO_TIMEOUTS))
			ssh.setTimeout(60000);
		ssh.connect(getUsername(), config.getServer(), config.getPort());
		LOG.info("Connected to {0}:{1}. Took {2}",config.getServer(), config.getPort(), System.currentTimeMillis() - startedConnecting);
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
