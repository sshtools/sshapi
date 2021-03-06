import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;
import net.sf.sshapi.util.XDetails;

/**
 * This example demonstrates X11 forwarding.
 */
public class E05X11Forwarding {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		/*
		 * Before using X11 forwarding, first the X11 parameters must be
		 * determined and set on the configuration object.
		 * 
		 * In this case, we try to get the value from the environment variable.
		 * If that cannot be determined, the prompt for it.
		 * 
		 * The easiest way to do this is to use the supplied XDetails utility
		 * class. The examines the local environment for the best values to use
		 * (the success of this will depend on what operating system you are
		 * using)
		 * 
		 * The second thing that must be done you perform when opening the
		 * channel (see below)
		 */
		SshConfiguration config = new SshConfiguration();
		new XDetails().configure(config);

		// If XDetails doesn't work, you can set these manually
		// config.setX11Host("ahost");
		// config.setX11Port(6000);
		// config.setX11Cookie(new byte[] { ... });

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
			/*
			 * Now you must either create a shell or start a command. This will
			 * activate the X11 forwarding
			 */
			SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
			try {
				/*
				 * Now open the channel and join the streams to the console. At
				 * this point X11 forwarding should now be active
				 */
				shell.open();
				ExampleUtilities.joinShellToConsole(shell);
			} finally {
				shell.close();
			}
		} finally {
			client.disconnect();
		}

	}
}
