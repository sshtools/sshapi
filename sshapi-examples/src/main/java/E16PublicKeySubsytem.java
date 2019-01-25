import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Some servers support remote management of authorized keys. This will also
 * require such support in the provider.
 */
public class E16PublicKeySubsytem {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.PUBLIC_KEY_SUBSYSTEM);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		SshClient client = provider.createClient(config);
		ExampleUtilities.dumpClientInfo(client);

		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		client.connect(user, host, port, new ConsolePasswordAuthenticator());
		System.out.println("Remote identification: " + client.getRemoteIdentification());

		//
		System.out.println("list - list all keys");
		String cmd = "";

		try (SshPublicKeySubsystem subsys = client.publicKeySubsystem()) {
			while (true) {
				cmd = Util.prompt("Command: ");
				if (cmd.equals("list")) {
					SshPublicKey[] keys = subsys.list();
					if (keys == null) {
						System.out.println("No keys");
					} else {
						for (int i = 0; i < keys.length; i++) {
							System.out.println(printKey(keys[i]));
						}
					}
				} else {
					System.out.println("Invalid command");
				}
			}
		}
	}

	private static String printKey(SshPublicKey sshHostKey) throws SshException {
		return sshHostKey.getAlgorithm() + " " + sshHostKey.getBitLength() + " " + sshHostKey.getFingerprint();
	}
}
