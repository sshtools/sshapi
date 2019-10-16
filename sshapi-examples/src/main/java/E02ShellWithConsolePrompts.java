import net.sf.sshapi.Capability;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
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
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		
		// Create the client using that configuration
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			
			// We are now connected and authenticated
			ExampleUtilities.dumpClientInfo(client);
			System.out.println("Remote identification: " + client.getRemoteIdentification());
			try {
				
				// Create the simple shell
				SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
				
				// Listen for channel events
				if (client.getProvider().getCapabilities().contains(Capability.CHANNEL_DATA_EVENTS)) {
					shell.addDataListener((channel, direction, buf, off, len) -> {
						if (direction == SshDataListener.RECEIVED) {
							dataTransferredIn += len;
						} else {
							dataTransferredOut += len;
						}
					});
				}
				shell.addListener(new SshChannelListener<SshShell>() {
					public void opened(SshShell channel) {
						System.out.println("Shell channel opened!");
					}

					public void closing(SshShell channel) {
						System.out.println("Shell channel closing!");
					}

					public void closed(SshShell channel) {
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
				// If the provider supports events, we will have some simple
				// stats
				if (client.getProvider().getCapabilities().contains(Capability.CHANNEL_DATA_EVENTS)) {
					System.out.println("Session over. Received " + dataTransferredIn + " bytes, sent " + dataTransferredOut
							+ " bytes over the shell channel");
				}
			}
		}
	}
}
