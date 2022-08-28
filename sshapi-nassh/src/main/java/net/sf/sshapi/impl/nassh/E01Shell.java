package net.sf.sshapi.impl.nassh;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public final class E01Shell {
	public static void main(String[] arg) throws Exception {
		var username = "pi";
		var hostname = "pi4";
		var password = "raspberry".toCharArray();
		int port = 22;

		try (var client = new NaSshProvider().open(new SshConfiguration(), username, hostname, port,
				new SimplePasswordAuthenticator(password))) {
			try(var cmd = client.command("ls")) {
				cmd.getInputStream().transferTo(System.out);
			}
		}
	}
}
