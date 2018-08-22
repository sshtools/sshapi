import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.DefaultGSSAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates using GSSAPI to authenticate without a password.
 * You should have a basic understanding of how Kerberos works, and additional
 * setup may be required.
 */
public final class E19ShellUsingGSSAPI {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {

		System.out.println("***************************************************************");
		System.out.println("* You must have a valid ticket before running this example.   *");
		System.out.println("* On Linux/Unit with MIT kerberos, you would use kinit first. *");
		System.out.println("* On Windows, you would need to be logged in to your domain.  *");
		System.out.println("***************************************************************\n");
		System.out.println("NOTE :-\n");
		System.out.println("1. If you are using MIT Kerberos 1.8 or above, you must use Java 7 or above");
		System.out.println("2. Make sure you have Java unlimited strength encryption policy files installed");
		System.out.println("3. If you are using MIT Kerberos, your cache type must be '3'.");
		System.out.println("4. Use FULLY QUALIFIED host names, e.h myserver.mydomain.com.\n");

		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Check the provider can do GSSAPI and create a create
		SshConfiguration configuration = new SshConfiguration();
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(configuration);
		if (!provider.getCapabilities().contains(Capability.GSSAPI_AUTHENTICATION)) {
			throw new Exception("Provider is not capable of GSSAPI.");
		}

		// Connect and authenticate
		DefaultGSSAuthenticator authenticator = new DefaultGSSAuthenticator(user);
		authenticator.setDebug(true);
		try (SshClient client = provider.open(configuration, user, host, port, authenticator)) {

			// Create a shell on the server and join it to the console
			// Create the shell channel
			try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {

				/*
				 * Call the utility method to join the remote streams to the console streams
				 */
				ExampleUtilities.joinShellToConsole(shell);
			}
		}
	}
}
