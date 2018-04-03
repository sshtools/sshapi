import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates remote port forwarding.
 */
public class E07RemoteForwarding {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		// Create the client using that configuration
		SshClient client = config.createClient();
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
			SshPortForward remote = client.createRemoteForward(host, 8900, "www.javassh.com", 80);
			remote.open();

			try {
				System.out.println("Point your browser to http://" + host + ":8900, you should "
					+ "see the home page for JavaSSH. This connection will close in 2 minutes.");
				// Wait for two minutes
				Thread.sleep(120000);
			} finally {
				remote.close();
			}
		} finally {
			client.disconnect();
		}

	}
}
