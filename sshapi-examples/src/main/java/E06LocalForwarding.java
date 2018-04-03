import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates local port forwarding.
 */
public class E06LocalForwarding {
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

		// Create the client using that configuration.
		SshClient client = config.createClient();
		ExampleUtilities.dumpClientInfo(client);

		// Prompt for the host and username
		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Connect, authenticate, and start the simple shell
		client.connect(user, host, port);
		client.authenticate(new ConsolePasswordAuthenticator());

		// If our provider supports it, adds listen for the events as tunneled
		// connections become active
		if (client.getProvider().getCapabilities().contains(Capability.PORT_FORWARD_EVENTS)) {
			client.addPortForwardListener(new SshPortForwardListener() {

				public void channelOpened(int type, SshPortForwardTunnel channel) {
					System.out.println("Channel open: " + type + " / " + channel);

				}

				public void channelClosed(int type, SshPortForwardTunnel channel) {
					System.out.println("Channel closed: " + type + " / " + channel);
				}
			});
		}

		try {
			SshPortForward local = client.createLocalForward(null, 8900, "www.javassh.com", 80);
			local.open();

			// Wait for two minute
			try {
				System.out.println("Point your browser to http://localhost:8900, you should "
					+ "see the home page for JavaSSH. This connection will close in 2 minutes.");
				Thread.sleep(120000);
			} finally {
				local.close();
			}
		} finally {
			client.disconnect();
		}

	}
}
