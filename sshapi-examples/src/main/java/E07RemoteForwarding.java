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
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());

		String connectionSpec = Util.promptConnectionSpec();
		String host = Util.extractHostname(connectionSpec);
		String user = Util.extractUsername(connectionSpec);
		int port = Util.extractPort(connectionSpec);

		// Create the client using that configuration and connect and authenticate
		try (SshClient client = config.open(user, host, port, new ConsolePasswordAuthenticator())) {

			try (SshPortForward remote = client.remoteForward(host, 8900, "sshtools.com", 80)) {
				System.out.println("Point your browser to http://" + host + ":8900, you should "
						+ "see the home page for JavaSSH. This connection will close in 2 minutes.");
				// Wait for two minutes
				Thread.sleep(120000);
			}
		}

	}
}
