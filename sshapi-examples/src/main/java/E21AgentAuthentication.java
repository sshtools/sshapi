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
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.AGENT);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());
		
		// Locate and connect to agent
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Connecting to agent.");
		SshAgent agent = provider.connectToLocalAgent("Test", System.getenv("SSH_AUTH_SOCK"), SshAgent.UNIX_DOMAIN_AGENT_SOCKET_TYPE);
		System.out.println("Connected to agent.");

		// Create the client using that configuration
		SshClient client = config.createClient();
		
		ExampleUtilities.dumpClientInfo(client);

		// Prompt for the host and username
		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Connect
		client.connect(user, host, port);

		// Connect the client to the agent
		client.addChannelHandler(agent);
		
		// Authenticate
		client.authenticate(new SshAuthenticator[] { new DefaultAgentAuthenticator(agent) });

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
