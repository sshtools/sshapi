import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.net.SocketFactory;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates the use of
 * {@link SshClient#createTunneledSocketFactory()} to create a standard Java
 * {@link Socket} whose connection is actually made from the remote SSH server.
 * 
 */
public class E17TunneledSocketFactory {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.TUNNELED_SOCKET_FACTORY);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		SshClient client = provider.createClient(config);
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
			SocketFactory sf = client.createTunneledSocketFactory();

			/*
			 * Make a connection back to the SSH server we are connection from
			 * and read the first line of output. This could be any host that is
			 * accessible from the remote SSH server, localhost:22 is just used
			 * as we know something will be running there!
			 */
			Socket socket = sf.createSocket("localhost", 22);
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				System.out.println("SSH ident: " + reader.readLine());
			} finally {
				socket.close();
			}
		} finally {
			client.disconnect();
		}

	}
}
