package com.maverick.ssh.tests;

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
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.Logger.Level;

public class AbstractSshTest {
	static {
		PropertyConfigurator.configure(AbstractSshTest.class.getResource("/log4j.properties"));
		System.setProperty("sshapi.logLevel", "INFO");
		System.setProperty("sshapi.extendTimeouts", "true");
	}
	
	protected static SshTestConfiguration config;
	protected static ServerService server;
	protected ThreadLocal<Boolean> debug = new ThreadLocal<Boolean>();

	public AbstractSshTest() {
		debug.set(false);
	}

	protected void debug(boolean debug) {
		this.debug.set(debug);
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
					SshConfiguration.getLogger().log(Level.ERROR, "Task timed out.");
					ex.set(true);
					timedOut(runnable, timeout, tthread, dn);
				} catch (InterruptedException ie) {
					if (!dn.get())
						SshConfiguration.getLogger().log(Level.ERROR, "No longer waiting for task to end.");
				} catch (Exception e) {
				}
			}
		};
		tothread.setDaemon(true);
		tothread.start();
		try {
			runnable.call();
		} finally {
			dn.set(true);
			tothread.interrupt();
		}
	}

	protected void timedOut(Callable<Void> runnable, long timeout, Thread tthread, AtomicBoolean dn) throws InterruptedException {
		tthread.interrupt();
		Thread.sleep(timeout);
		if (!dn.get()) {
			SshConfiguration.getLogger().log(Level.ERROR, "The test " + runnable.toString() + " (thread " + tthread
					+ ") appears to have hung, and cannot be interrupted. Exiting the JVM with error status.");
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
			SshConfiguration.getLogger().log(Level.INFO, "Provider");
			SshConfiguration.getLogger().log(Level.INFO, "    Name: " + prov.getName());
			SshConfiguration.getLogger().log(Level.INFO, "    Version: " + prov.getVersion());
			SshConfiguration.getLogger().log(Level.INFO, "    Vendor: " + prov.getVendor());
			SshConfiguration.getLogger().log(Level.INFO, "    Class: " + prov.getClass().getName());
			SshConfiguration.getLogger().log(Level.INFO, "Capabilities: " + prov.getCapabilities());
			SshConfiguration.getLogger().log(Level.INFO, "Ciphers: " + prov.getSupportedCiphers(SshConfiguration.SSH2_ONLY));
			SshConfiguration.getLogger().log(Level.INFO, "MAC: " + prov.getSupportedMAC());
			SshConfiguration.getLogger().log(Level.INFO, "Compression: " + prov.getSupportedCompression());
			SshConfiguration.getLogger().log(Level.INFO, "Key Exchange: " + prov.getSupportedKeyExchange());
			SshConfiguration.getLogger().log(Level.INFO, "Public Key: " + prov.getSupportedPublicKey());
			SshConfiguration.getLogger().log(Level.INFO,
					String.format("Provider initialisation took %d", System.currentTimeMillis() - timestamp));
			server = config.getServerService();
			server.start();
			SshConfiguration.getLogger().log(Level.INFO, "-----------------------------");
			SshConfiguration.getLogger().log(Level.INFO, "Stty                         ");
			SshConfiguration.getLogger().log(Level.INFO, "");
			ProcessBuilder pb = new ProcessBuilder("stty", "-a");
			Process process = pb.start();
			Util.monitorProcessOut(process);
			Util.monitorProcessErr(process);
			// assertEquals("Stty must return 0", 0, process.waitFor());
			process.waitFor();
			SshConfiguration.getLogger().log(Level.INFO, "-----------------------------");
			SshConfiguration.getLogger().log(Level.INFO, "Environment                  ");
			SshConfiguration.getLogger().log(Level.INFO, "");
			for (Map.Entry<String, String> e : System.getenv().entrySet()) {
				SshConfiguration.getLogger().log(Level.INFO, "   " + e.getKey() + "=" + e.getValue());
			}
		} catch (Exception e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Failed to setup.", e);
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
