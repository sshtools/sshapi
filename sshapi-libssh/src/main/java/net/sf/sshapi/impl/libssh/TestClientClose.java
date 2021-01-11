package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsoleLogger;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

public class TestClientClose {
	static {
		((ConsoleLogger)SshConfiguration.getLogger()).setDefaultLevel(Level.INFO);
	}

	public static void main(String[] args) throws IOException {
		
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());

		// Create the client using that configuration, then connect and authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			try (SshPortForward local = client.localForward(null, 19999, "localhost", 9999)) {
				try (Socket s = new Socket("localhost", 19999)) {
					OutputStream out = s.getOutputStream();
					InputStream in = s.getInputStream();
					out.write("HELLO".getBytes());
					out.close();
					byte[] b = new byte[20];
					int r = in.read(b);
					System.out.println("Got: " + r + " : " + new String(b, 0, r));
					r = in.read();
					if (r != -1) {
						System.out.println("Unexpected byte!");
					}
				}
			}
		}
		
		
	}
}
