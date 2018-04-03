import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshStreamChannel;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates executing a command instead of a shell.
 */
public class E04ExecuteCommand {
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

		// Connect, authenticate, and start the simple shell
		client.connect(user, host, port);
		client.authenticate(new ConsolePasswordAuthenticator());
		try {
			// Execute the command and read back its output
			SshStreamChannel channel = client.createCommand("ls /etc");
			channel.open();
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}
			} finally {
				channel.close();
			}
		} finally {
			client.disconnect();
		}

	}
}
