import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

/**
 * This example demonstrates the simplest use of the API, opening up a
 * connection, creating a shell, and joining the channel streams to standard
 * input / output to create a simple remote shell application.
 */
public final class E01Shell {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Set the basic configuration for this conneciton
		// Put your own detains in here
		String username = "CHANGE_THIS";
		String hostname = "CHANGE_THIS";
		char[] password = "CHANGE_THIS".toCharArray();
		int port = 22;

		// Create a new client using default configuration
		SshConfiguration configuration = new SshConfiguration();
		SshClient client = configuration.createClient();

		/*
		 * Look at the source of ExampleUtilities to see how to query a provider
		 * for its capabilities
		 */
		ExampleUtilities.dumpClientInfo(client);

		// Connect and authenticate
		client.connect(username, hostname, port);
		client.authenticate(new SimplePasswordAuthenticator(password));

		// Create a shell on the server and join it to the console
		try {
			// Create the shell channel
			SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);

			/*
			 * Open the shell channel. All channels must be opened once created
			 * and closed when finished with
			 */
			try {
				shell.open();

				/*
				 * Call the utility method to join the remote streams to the
				 * console streams
				 */
				ExampleUtilities.joinShellToConsole(shell);
			} finally {
				shell.close();
			}
		} finally {
			// Always remember to close the client when finished with
			client.disconnect();
		}
	}
}
