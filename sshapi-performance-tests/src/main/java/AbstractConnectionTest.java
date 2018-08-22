import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;

import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.DumbHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

abstract class AbstractConnectionTest {

	final static org.slf4j.Logger log;

	protected static Properties PROPERTIES = new Properties();
	static {
		File propertyFile = new File("ssh-test.properties");
		try {
			FileInputStream fin = new FileInputStream(propertyFile);
			try {
				PROPERTIES.load(fin);
			} finally {
				fin.close();
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
		SshConfiguration.setLogger(new Logger() {
			@Override
			public void log(Level level, String message, Throwable exception) {
				switch (level) {
				case INFO:
					log.info(message, exception);
					break;
				case ERROR:
					log.error(message, exception);
					break;
				case DEBUG:
					log.debug(message, exception);
					break;
				case WARN:
					log.warn(message, exception);
					break;
				default:
					break;
				}
			}

			@Override
			public void log(Level level, String message) {
				switch (level) {
				case INFO:
					log.info(message);
					break;
				case ERROR:
					log.error(message);
					break;
				case DEBUG:
					log.debug(message);
					break;
				case WARN:
					log.warn(message);
					break;
				default:
					break;
				}
			}

			@Override
			public boolean isLevelEnabled(Level required) {
				return true;
			}
		});
		log = LoggerFactory.getLogger(AbstractConnectionTest.class);
	}
	protected SshConfiguration configuration;
	protected Map<String, List<Long>> times = new HashMap<>();
	protected Map<String, Throwable> exceptions = new HashMap<>();

	public AbstractConnectionTest() throws IOException {
		this("ConnectionTest");
	}

	public AbstractConnectionTest(String propertyPrefix) throws IOException {
		configuration = new SshConfiguration();
		configuration.setHostKeyValidator(new DumbHostKeyValidator());
		configuration.setPreferredClientToServerCipher("aes128-ctr");
		configuration.setPreferredServerToClientCipher("aes128-ctr");
		configuration.setPreferredClientToServerCompression("none");
		configuration.setPreferredServerToClientCompression("none");
		configuration.setPreferredPublicKey(SshConfiguration.PUBLIC_KEY_SSHRSA);
	}

	public SshProvider[] getAllConfiguredProviders() {
		List<String> include = parseList(PROPERTIES.getProperty("include", ""));
		List<String> exclude = parseList(PROPERTIES.getProperty("exclude", ""));
		List<SshProvider> providers = new ArrayList<>();
		SshProvider[] alLProviders = DefaultProviderFactory.getAllProviders();
		for (int i = 0; i < alLProviders.length; i++) {
			SshProvider provider = alLProviders[i];
			if (!exclude.contains(provider.getName())
					&& (include.size() == 0 || include.contains(provider.getName()))) {
				providers.add(provider);
			}
		}
		return (SshProvider[]) providers.toArray(new SshProvider[0]);
	}

	private List<String> parseList(String list) {
		list = list.trim();
		if (list.equals("")) {
			return new ArrayList<>();
		}
		return Arrays.asList(list.split(","));
	}

	public void start() throws Exception {
		int repeats = Integer.parseInt(PROPERTIES.getProperty("repeats", "10"));
		int warmUps = Integer.parseInt(PROPERTIES.getProperty("warmUps", "1"));
		SshConfiguration.getLogger().log(Level.INFO, "Warming up");
		for (int i = 0; i < warmUps; i++) {
			singleRun();
		}
		SshConfiguration.getLogger().log(Level.INFO,
				"Warmed up, starting actual run of " + repeats + " runs for each provider");
		resetStats();
		for (int i = 0; i < repeats; i++) {
			singleRun();
		}
		SshProvider[] providers = getAllConfiguredProviders();
		for (int i = 0; i < providers.length; i++) {
			SshProvider provider = providers[i];
			List<Long> providerTimes = times.get(provider.getName());
			StringBuilder sb = new StringBuilder();
			try (Formatter formatter = new Formatter(sb)) {
				if (providerTimes == null) {
					Throwable t = exceptions.get(provider.getName());
					formatter.format("%20s Failed. %s", new Object[] { provider.getName(),
							t == null || t.getMessage() == null ? "" : t.getMessage() });
				} else {
					long total = 0;
					for (Long t : providerTimes)
						total += t;
					long avg = total / providerTimes.size();
					formatter.format("%20s Total: %6d  Avg per run: %6d",
							new Object[] { provider.getName(), new Long(total), new Long(avg) });
				}
			}
			System.out.println(sb);
		}
	}

	void singleRun() throws Exception {
		SshProvider[] providers = getAllConfiguredProviders();
		for (int i = 0; i < providers.length; i++) {
			long started = System.currentTimeMillis();
			SshProvider provider = providers[i];
			try {
				doProvider(provider);
				long finished = System.currentTimeMillis();
				List<Long> providerTimes = times.get(provider.getName());
				if (providerTimes == null) {
					providerTimes = new ArrayList<>();
					times.put(provider.getName(), providerTimes);
				}
				providerTimes.add(new Long(finished - started));
			} catch (Exception e) {
				log.error(String.format("Provider %s failed.", provider.getName()), e);
				exceptions.put(provider.getName(), e);
			}
		}
	}

	void resetStats() {
		times.clear();
	}

	protected void time(SshProvider provider, Runnable runnable) throws Exception {
		long started = System.currentTimeMillis();
		try {
			runnable.run();
		} catch (Error e) {
			if (e.getCause() != null && e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else {
				throw e;
			}
		}
		long finished = System.currentTimeMillis();
		List<Long> providerTimes = times.get(provider.getName());
		if (providerTimes == null) {
			providerTimes = new ArrayList<>();
			times.put(provider.getName(), providerTimes);
		}
		providerTimes.add(new Long(finished - started));
	}

	protected void doProvider(SshProvider provider) throws Exception {
		try (SshClient client = connect(provider)) {
			time(provider, new Runnable() {
				public void run() {
					try {
						doConnection(client);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		}
	}

	protected final SshClient connect(SshProvider provider) throws SshException {
		SshConfiguration.getLogger().log(Level.INFO, "\tProvider " + provider.getName());
		String password = PROPERTIES.getProperty("password");
		if (password == null) {
			throw new IllegalArgumentException("Password must be provided in properties file.");
		}
		try {
			return provider.open(configuration, PROPERTIES.getProperty("username", System.getProperty("user.name")),
					PROPERTIES.getProperty("hostname", "localhost"),
					Integer.parseInt(PROPERTIES.getProperty("port", "22")),
					// new SimpleKeyboardInteractiveAuthenticator(password)
					new SimplePasswordAuthenticator(password.toCharArray()));
		} catch (SshException sshe) {
			throw sshe;
		}
	}

	protected void doConnection(SshClient client) throws Exception {
		// For sub-classes to do something useful with a client
	}
}
