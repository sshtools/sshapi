import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates using the {@link SshHostKeyManager} to provide more
 * functionality to a {@link SshHostKeyValidator}. When passed the key manager,
 * the validator can check keys against those stored, and either offer to add
 * them to the list of 'known hosts', or reject if there is a key mismatch
 */
public class E13ExtendedHostKeyValidation {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.HOST_KEY_MANAGEMENT);

		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Got provider " + provider.getClass());
		SshHostKeyManager mgr = provider.createHostKeyManager(config);

		/*
		 * Configure a host key validator. Pass the host key manager so the
		 * validator can add new host keys and check for mismatches against
		 * stored ones
		 */
		config.setHostKeyValidator(new ConsoleHostKeyValidator(mgr));

		// Now create a client
		SshClient client = provider.createClient(config);
		ExampleUtilities.dumpClientInfo(client);

		// Prompt for the host and username
		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Connect, authenticate
		client.connect(user, host, port);
		client.authenticate(new ConsolePasswordAuthenticator());

		try {
			SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
			try {
				shell.open();
				ExampleUtilities.joinShellToConsole(shell);
			} finally {
				shell.close();
			}
		} finally {
			client.disconnect();
		}
	}
}
