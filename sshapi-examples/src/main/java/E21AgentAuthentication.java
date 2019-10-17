import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
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

		// Connect and authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new DefaultAgentAuthenticator())) {
			ExampleUtilities.dumpClientInfo(client);
			try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				ExampleUtilities.joinShellToConsole(shell);
			}	
		}
	}
}
