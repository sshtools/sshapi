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
package com.maverick.ssh.tests;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;

import com.maverick.ssh.tests.ServerService.AuthenticationMethod;

import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger;
import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;

public class AbstractSshTest {

	protected static final Logger LOG;
	
	static {
		PropertyConfigurator.configure(AbstractSshTest.class.getResource("/log4j.properties"));
		System.setProperty("sshapi.logLevel", "INFO");
		LOG = SshConfiguration.getLogger();
		System.setProperty("sshapi.extendTimeouts", "false");
	}
	
	protected static SshTestConfiguration config;
	protected static ServerService server;
	protected ThreadLocal<Boolean> debug = new ThreadLocal<Boolean>();
	protected Map<Thread, Boolean> timedOut = Collections.synchronizedMap(new HashMap<>());

	public AbstractSshTest() {
		debug.set(false);
	}

	protected void debug(boolean debug) {
		this.debug.set(debug);
	}
	
	protected boolean isTimedOut() {
		return Boolean.TRUE.equals(timedOut.get(Thread.currentThread()));
	}

	/**
	 * Run a task that might hang the server if it fails. It will first try to
	 * interrupt the task. If this doesn't work, then eventually the JVM will
	 * exit with an error code
	 * 
	 * @param runnable runnable
	 * @param timeout initial timeout
	 * @throws Exception on error
	 */
	protected void timeout(Callable<Void> runnable, long timeout) throws Exception {
		Thread tthread = Thread.currentThread();
		timedOut.put(tthread, false);
		AtomicBoolean ex = new AtomicBoolean();
		AtomicBoolean dn = new AtomicBoolean();
		Thread tothread = new Thread("TimeoutThread") {
			@Override
			public void run() {
				try {
					if ("true".equals(System.getProperty("sshapi.extendTimeouts"))) {
						Thread.sleep(timeout * 10000);
					} else
						Thread.sleep(timeout);
					SshConfiguration.getLogger().error("Task timed out.");
					ex.set(true);
					timedOut.put(tthread, true);
					timedOut(runnable, timeout, tthread, dn);
				} catch (InterruptedException ie) {
					if (!dn.get())
						SshConfiguration.getLogger().error("No longer waiting for task to end.");
				} catch (Exception e) {
				}
			}
		};
		tothread.setDaemon(true);
		tothread.start();
		try {
			runnable.call();
		} finally {
			timedOut.remove(tthread);
			dn.set(true);
			tothread.interrupt();
		}
	}

	protected void timedOut(Callable<Void> runnable, long timeout, Thread tthread, AtomicBoolean dn) throws InterruptedException {
		tthread.interrupt();
		Thread.sleep(timeout);
		if (!dn.get()) {
			SshConfiguration.getLogger().error("The test {0} (thread {1} appears to have hung, and cannot be interrupted). Exiting the JVM with error status.", runnable.toString(), tthread);
			System.exit(9);
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			config = SshTestConfiguration.get();
			String provider = config.getProvider();
			if (!provider.equals("")) {
				try {
					Class.forName(provider);
					System.setProperty(DefaultProviderFactory.PROVIDER_CLASS_NAME, provider);
				} catch (Exception e) {
					SshProvider prov = DefaultProviderFactory.getProviderByName(provider);
					if (prov == null)
						throw new IllegalStateException(
								String.format("Provider not found by either name or class name for %s.", provider));
					System.setProperty(DefaultProviderFactory.PROVIDER_CLASS_NAME, prov.getClass().getName());
				}
			}
			long timestamp = System.currentTimeMillis();
			SshProvider prov = DefaultProviderFactory.getInstance().getProvider(new SshConfiguration());
			LOG.info("SSHAPI: " + Ssh.version());
			LOG.info("Provider");
			LOG.info("    Name: " + prov.getName());
			LOG.info("    Version: " + prov.getVersion());
			LOG.info("    Vendor: " + prov.getVendor());
			LOG.info("    Class: " + prov.getClass().getName());
			LOG.info("Capabilities: " + prov.getCapabilities());
			LOG.info("Ciphers: " + prov.getSupportedCiphers(SshConfiguration.SSH2_ONLY));
			LOG.info("MAC: " + prov.getSupportedMAC());
			LOG.info("Compression: " + prov.getSupportedCompression());
			LOG.info("Key Exchange: " + prov.getSupportedKeyExchange());
			LOG.info("Public Key: " + prov.getSupportedPublicKey());
			LOG.info("Provider initialisation took {0}", System.currentTimeMillis() - timestamp);
			server = config.getServerService();
			server.start();
			LOG.info("-----------------------------");
			LOG.info("Stty                         ");
			LOG.info("");
			ProcessBuilder pb = new ProcessBuilder("stty", "-a");
			Process process = pb.start();
			Util.monitorProcessOut(process);
			Util.monitorProcessErr(process);
			// assertEquals("Stty must return 0", 0, process.waitFor());
			process.waitFor();
			LOG.info("-----------------------------");
			LOG.info("Environment                  ");
			LOG.info("");
			for (Map.Entry<String, String> e : System.getenv().entrySet()) {
				LOG.info("   " + e.getKey() + "=" + e.getValue());
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().error("Failed to setup.", e);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server.stop();
	}

	/**
	 * <p>
	 * Assert the server has a list of required capabilities. An empty list
	 * indicates the test requires no optional server features.
	 * 
	 * @param capabilities
	 */
	protected void assertServerCapabilities(ServerCapability... capabilities) {
		Assume.assumeTrue("Must have capabilities " + capabilities, checkCapabilities(capabilities));
	}

	/**
	 * <p>
	 * Check the server has a list of required capabilities. An empty list
	 * indicates the test requires no optional server features.
	 * 
	 * <p>
	 * At the moment this will throw an assertion exception if the capabilities
	 * aren't met.
	 * 
	 * @param capabilities
	 * @return capabilities are met
	 */
	protected boolean checkCapabilities(ServerCapability... capabilities) {
		// Looks a bit daft, but we can switch between failing and passing the
		// tests by commenting out the assert below
		boolean caps = doCheckCapabilities(capabilities);
		if (!caps) {
			Assume.assumeTrue("Must have capabilities " + Util.toString((Object[]) capabilities), false);
		}
		return caps;
	}

	private AuthenticationMethod[] currentMethods;

	/**
	 * De-configure previously configured authentication methods (
	 * {@link #configureForMethods(AuthenticationMethod...)}) and restart the
	 * server.
	 * 
	 * @throws Exception
	 */
	protected void deconfigureForMethods() throws Exception {
		ServerService serverService = config.getServerService();
		for (AuthenticationMethod m : currentMethods) {
			serverService.removeRequiredAuthentication(m);
		}
		serverService.restart();
	}

	/**
	 * Configures the server with required authentication methods and restart
	 * it. This should be used in @Test methods and it will remember the methods
	 * configured so a subsequence call to {@link #deconfigureForMethods()} will
	 * remove them.
	 * 
	 * @param methods
	 * @throws Exception
	 */
	protected void configureForMethods(AuthenticationMethod... methods) throws Exception {
		this.currentMethods = methods;
		ServerService serverService = config.getServerService();
		for (AuthenticationMethod m : methods) {
			serverService.addRequiredAuthentication(m);
		}
		serverService.restart();
	}

	private boolean doCheckCapabilities(ServerCapability... capabilities) {
		List<ServerCapability> caps = config.getServerCapabilities();
		for (ServerCapability c : capabilities) {
			if (!caps.contains(c)) {
				System.out.println("**** INFORMATION: This test is skipped because it requires the " + c + " capability");
				return false;
			}
		}
		return true;
	}
}
