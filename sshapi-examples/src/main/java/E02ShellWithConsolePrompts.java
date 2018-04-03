import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshDataProducingComponent;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E01Shell}, except the connection details and
 * credentials are not hard coded. Instead, they are prompted for on the
 * console.
 * <p>
 * It also demonstrates listening to channel events.
 * 
 */
public class E02ShellWithConsolePrompts {

	static int dataTransferredIn = 0;
	static int dataTransferredOut = 0;

	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.setHostKeyValidator(new ConsoleHostKeyValidator());

		// Also display banner messages
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
		System.out.println("Remote identification: " + client.getRemoteIdentification());
		client.authenticate(new ConsolePasswordAuthenticator());

		try {
			// Create the simple shell
			SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
			// SshShell shell = client.createShell(null, 80, 24, 0, 0, null);

			// Listen for channel events
			shell.addDataListener(new SshDataListener() {

				public void data(SshDataProducingComponent channel, int direction, byte[] buf, int off, int len) {
					if (direction == SshDataListener.RECEIVED) {
						dataTransferredIn += len;
					} else {
						dataTransferredOut += len;
					}
				}
			});
			shell.addListener(new SshLifecycleListener() {

				public void opened(SshLifecycleComponent channel) {
					System.out.println("Shell channel opened!");
				}

				public void closing(SshLifecycleComponent channel) {
					System.out.println("Shell channel closing!");
				}

				public void closed(SshLifecycleComponent channel) {
					System.out.println("Shell channel closed!");
				}
			});

			try {
				shell.open();
				ExampleUtilities.joinShellToConsole(shell);
			} finally {
				shell.close();
			}
		} finally {
			client.disconnect();

			// If the provider supports events, we will have some simple stats
			if (client.getProvider().getCapabilities().contains(Capability.CHANNEL_DATA_EVENTS)) {
				System.out.println("Session over. Received " + dataTransferredIn + " bytes, sent " + dataTransferredOut
					+ " bytes over the shell channel");
			}
		}
	}
}
