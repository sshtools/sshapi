import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.DefaultAgentAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Authenticates using keys obtained from the local agent.
 */
public class E21AgentAuthentication {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		config.addRequiredCapability(Capability.AGENT);

		// Locate and connect to agent
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Connecting to agent.");
		SshAgent agent = provider.connectToLocalAgent("Test");
		System.out.println("Connected to agent.");

		// Create the client using that configuration
		SshClient client = config.createClient();

		ExampleUtilities.dumpClientInfo(client);

		// Connect
		client.connect(Util.promptConnectionSpec());

		// Connect the client to the agent
		client.addChannelHandler(agent);

		// Authenticate
		client.authenticate(new SshAuthenticator[] { new DefaultAgentAuthenticator(agent) });

		try {
			try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				ExampleUtilities.joinShellToConsole(shell);
			}
		} finally {
			client.close();
		}
	}
}
