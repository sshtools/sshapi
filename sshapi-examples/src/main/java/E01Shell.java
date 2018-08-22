import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshClient;
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

		// Connect and authenticate
		try(SshClient client = Ssh.open(username, hostname, port, new SimplePasswordAuthenticator(password))) {

			/*
			 * Look at the source of ExampleUtilities to see how to query a provider
			 * for its capabilities
			 */
			ExampleUtilities.dumpClientInfo(client);
			
			// Create a shell on the server and join it to the console
			try(SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				/*
				 * Call the utility method to join the remote streams to the
				 * console streams
				 */
				ExampleUtilities.joinShellToConsole(shell);
			} 
		} 
	}
}
