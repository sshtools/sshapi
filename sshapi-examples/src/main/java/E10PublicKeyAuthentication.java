import java.io.File;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.PEMFilePublicKeyAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E01ShellWithConsolePrompts}, except public key
 * authentication is used instead of password.
 * 
 */
public class E10PublicKeyAuthentication {
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
		// Prompt for location of private key
		File pemFile = new File(Util.prompt("Private key file",
				System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"));
		// Connect, authenticate
		client.connect(user, host, port);
		try {
			/*
			 * To use public key authentication, we need to provide a different
			 * authenticator to all the other examples. In this case, we provide
			 * the key authenticator and a password authenticator
			 * 
			 * Note that a ConsolePasswordAuthenticator object is also created.
			 * This class implements SshPasswordPrompt, that the public key
			 * authenticator uses to prompt for a passphrase if one is required
			 */
			SshAuthenticator[] authenticators = new SshAuthenticator[] {
					new PEMFilePublicKeyAuthenticator(new ConsolePasswordAuthenticator(), pemFile), };
			
			/* We use this example to demonstrate being in control of the authentication loop.
			 * For most cases you wouldn't need to do this, just pass in the authenticators
			 * in the connect() method.
			 * 
			 * Here we demonstrate attempting to authenticate 3 times. Depending on the authenticators
			 * in use, the may be called to request a password again, or a key again etc.
			 */
			for (int i = 0; i < 3 && client.authenticate(authenticators); i++)
				;
			if(!client.isAuthenticated())
				throw new SshException(SshException.AUTHENTICATION_FAILED);
			
			/* Start a simple shell */
			try (SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null)) {
				ExampleUtilities.joinShellToConsole(shell);
			}
		} finally {
			client.closeQuietly();
		}
	}
}
