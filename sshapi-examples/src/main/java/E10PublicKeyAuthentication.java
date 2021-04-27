/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
import java.io.File;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;
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
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		
		// Create the client using that configuration
		SshClient client = config.createClient();
		ExampleUtilities.dumpClientInfo(client);
		
		// Prompt for the host and username
		String connectionSpec = Util.promptConnectionSpec();
		
		// Prompt for location of private key
		File pemFile = new File(Util.prompt("Private key file",
				System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"));
		
		// Connect, authenticate
		client.connect(connectionSpec);
		try {
			SshAuthenticator[] authenticators = new SshAuthenticator[] {
					new DefaultPublicKeyAuthenticator(new ConsolePasswordAuthenticator(), pemFile), };
			
			/* We use this example to demonstrate being in control of the authentication loop.
			 * For most cases you wouldn't need to do this, just pass in the authenticators
			 * in the connect() method.
			 * 
			 * Here we demonstrate attempting to authenticate 3 times. Depending on the authenticators
			 * in use, the may be called to request a password again, or a key again etc.
			 */
			for (int i = 0; i < 3 && !client.authenticate(authenticators); i++)
				;
			if(!client.isAuthenticated())
				throw new SshException(SshException.AUTHENTICATION_FAILED);
			
			/* Start a simple shell */
			try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {
				ExampleUtilities.joinShellToConsole(shell);
			}
		} finally {
			client.closeQuietly();
		}
	}
}
