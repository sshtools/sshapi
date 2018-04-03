import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.DumbHostKeyValidator;
import net.sf.sshapi.util.SimpleKeyboardInteractiveAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

abstract class AbstractConnectionTest {

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
		// BasicConfigurator.configure();
	}
	private SshConfiguration configuration;

	protected Map times = new HashMap();

	public AbstractConnectionTest() throws IOException {
		this("ConnectionTest");
	}

	public AbstractConnectionTest(String propertyPrefix) throws IOException {

		configuration = new SshConfiguration();
		configuration.setHostKeyValidator(new DumbHostKeyValidator());
		configuration.setPreferredClientToServerCipher("blowfish-cbc");
		configuration.setPreferredServerToClientCipher("blowfish-cbc");
		configuration.setPreferredClientToServerCompression("none");
		configuration.setPreferredServerToClientCompression("none");
		configuration.setPreferredPublicKey(SshConfiguration.PUBLIC_KEY_SSHRSA);

		configuration.addRequiredCapability(Capability.SCP);
	}

	public SshProvider[] getAllConfiguredProviders() {
		List include = parseList(PROPERTIES.getProperty("include", ""));
		List exclude = parseList(PROPERTIES.getProperty("exclude", ""));
		List providers = new ArrayList();
		SshProvider[] alLProviders = DefaultProviderFactory.getAllProviders();
		for (int i = 0; i < alLProviders.length; i++) {
			SshProvider provider = alLProviders[i];
			if (!exclude.contains(provider.getName()) && (include.size() == 0 || include.contains(provider.getName()))) {
				providers.add(provider);
			}
		}
		return (SshProvider[]) providers.toArray(new SshProvider[0]);
	}

	private List parseList(String list) {
		list = list.trim();
		if (list.equals("")) {
			return new ArrayList();
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
		SshConfiguration.getLogger().log(Level.INFO, "Warmed up, starting actual run of " + repeats + " runs for each provider");
		resetStats();
		for (int i = 0; i < repeats; i++) {
			singleRun();
		}

		SshProvider[] providers = getAllConfiguredProviders();
		for (int i = 0; i < providers.length; i++) {
			SshProvider provider = providers[i];
			List providerTimes = (List) times.get(provider);
			long total = 0;
			for (Iterator it = providerTimes.iterator(); it.hasNext();) {
				Long t = (Long) it.next();
				total += t.longValue();
			}
			long avg = total / providerTimes.size();
			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb);
			formatter.format("%20s Total: %6d  Avg per run: %6d",
				new Object[] { provider.getName(), new Long(total), new Long(avg) });
			System.out.println(sb);
		}

	}

	void singleRun() throws Exception {
		SshProvider[] providers = getAllConfiguredProviders();
		for (int i = 0; i < providers.length; i++) {
			long started = System.currentTimeMillis();
			SshProvider provider = providers[i];
			doProvider(provider);
			long finished = System.currentTimeMillis();
			List providerTimes = (List) times.get(provider);
			if (providerTimes == null) {
				providerTimes = new ArrayList();
				times.put(provider, providerTimes);
			}
			providerTimes.add(new Long(finished - started));
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
		List providerTimes = (List) times.get(provider);
		if (providerTimes == null) {
			providerTimes = new ArrayList();
			times.put(provider, providerTimes);
		}
		providerTimes.add(new Long(finished - started));

	}

	protected void doProvider(SshProvider provider) throws Exception {
		final SshClient client = connect(provider);
		try {
			time(provider, new Runnable() {
				public void run() {
					try {
						doConnection(client);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} finally {
			client.disconnect();
		}
	}

	protected final SshClient connect(SshProvider provider) throws SshException {
		SshConfiguration.getLogger().log(Level.INFO, "\tProvider " + provider.getName());
		SshClient client = provider.createClient(configuration);
		client.connect(PROPERTIES.getProperty("username", System.getProperty("user.name")),
			PROPERTIES.getProperty("hostname", "localhost"), Integer.parseInt(PROPERTIES.getProperty("port", "22")));
		try {
			String password = PROPERTIES.getProperty("password");
			if (password == null) {
				throw new IllegalArgumentException("Password must be provided in properties file.");
			}
			client.authenticate(new SshAuthenticator[] { 
				new SimpleKeyboardInteractiveAuthenticator(password) });
		} catch (SshException sshe) {
			client.disconnect();
			throw sshe;
		}
		return client;
	}

	protected void doConnection(SshClient client) throws Exception {
		// For sub-classes to do something useful with a client
	}
}
