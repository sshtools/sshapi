import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates forwarding channels. These are what local port forwards are based
 * on, but do not actually set up a local socket. Instead, the I/O stream may be used in whichever
 * way you wish. {@link SshClient#createTunneledSocketFactory()} implementations will most likely
 * be built on this.
 */
public class E25ForwardingChannel {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());

		// Create the client using that configuration, then connect and authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {

			try (SshChannel local = client.forwardingChannel("www.jadaptive.com", 80)) {
				
				/* Make a very simple HTTP request */
				OutputStream out = local.getOutputStream();
				out.write("GET / HTTP/1.1\r\nHost:www.jadaptive.com\r\nConnection: close\r\n\r\n".getBytes());
				out.flush();
				
				/* Read back to raw response */
				InputStream in = local.getInputStream();
				ExampleUtilities.copy(in, System.out);
			}
		}

	}
}
