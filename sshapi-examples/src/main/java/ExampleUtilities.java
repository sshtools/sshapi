import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;

class ExampleUtilities {
	
	static String extractHostname(String connectionSpec) {
		connectionSpec = connectionSpec.substring(connectionSpec.indexOf('@') + 1);
		int idx = connectionSpec.indexOf(':');
		if (idx != -1) {
			connectionSpec = connectionSpec.substring(0, idx);
		}
		return connectionSpec;
	}
	
	static String extractUsername(String connectionSpec) {
		return connectionSpec.substring(0, connectionSpec.indexOf('@'));
	}
	
	static int extractPort(String connectionSpec) {
		connectionSpec = connectionSpec.substring(connectionSpec.indexOf('@') + 1);
		int idx = connectionSpec.indexOf(':');
		if (idx != -1) {
			return Integer.parseInt(connectionSpec.substring(idx + 1));
		}
		return 22;
	}

	static void dumpClientInfo(SshClient client) {
		System.out.println("Provider: " + client.getProvider().getClass().getName());
		System.out.println("Client: " + client.getClass().getName());
		System.out.println("Capabilities: " + client.getProvider().getCapabilities());
		int protocolVersion = client.getConfiguration().getProtocolVersion();
		System.out.println("Ciphers: " + client.getProvider().getSupportedCiphers(protocolVersion));
		if (protocolVersion != SshConfiguration.SSH1_ONLY) {
			System.out.println("MAC: " + client.getProvider().getSupportedMAC());
			System.out.println("Compression: " + client.getProvider().getSupportedCompression());
			System.out.println("Key Exchange: " + client.getProvider().getSupportedKeyExchange());
			System.out.println("Public Key: " + client.getProvider().getSupportedPublicKey());
		}
	}

	static void joinShellToConsole(final SshShell channel) throws IOException, SshException {
		new Thread() {
			public void run() {
				try {
					Util.joinStreams(channel.getExtendedInputStream(), System.err);
				} catch (Exception e) {
				}
			}
		}.start();
		Thread readInThread = new Thread() {
			public void run() {
				try {
					Util.joinStreams(System.in, channel.getOutputStream());
				} catch (Exception e) {
				}
			}
		};
		readInThread.setDaemon(true);
		readInThread.start();
		InputStream in = channel.getInputStream();
		OutputStream out = System.out;
		int r;
		while ((r = in.read()) != -1) {
			out.write(r);
		}
	}
}
