import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.Util;

class ExampleUtilities {
	/**
	 * Default buffer size for stream utility methods
	 */
	public static int BUFFER_SIZE = 8192;

	static void dumpClientInfo(SshClient client) {
		System.out.println("Provider");
		System.out.println("    Name: " + client.getProvider().getName());
		System.out.println("    Version: " + client.getProvider().getVersion());
		System.out.println("    Vendor: " + client.getProvider().getVendor());
		System.out.println("    Class: " + client.getProvider().getClass().getName());
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
					channel.getInputStream().close();
				} catch (Exception e) {
				}
			}
		};
		readInThread.setDaemon(true);
		readInThread.start();
		Util.joinStreams(channel.getInputStream(), System.out);
		System.out.println("Left Console Input");
		try {
			readInThread.join(1000);
		} catch (InterruptedException ie) {
			readInThread.interrupt();
		}
	}

	/**
	 * Copy from an input stream to an output stream. It is up to the caller to
	 * close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		copy(in, out, -1);
	}

	/**
	 * Copy the specified number of bytes from an input stream to an output
	 * stream. It is up to the caller to close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param count number of bytes to copy
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out, long count) throws IOException {
		copy(in, out, count, BUFFER_SIZE);
	}

	/**
	 * Copy the specified number of bytes from an input stream to an output
	 * stream. It is up to the caller to close the streams.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param count number of bytes to copy
	 * @param bufferSize buffer size
	 * @throws IOException on any error
	 */
	public static void copy(InputStream in, OutputStream out, long count, int bufferSize) throws IOException {
		byte buffer[] = new byte[bufferSize];
		int i = bufferSize;
		if (count >= 0) {
			while (count > 0) {
				if (count < bufferSize)
					i = in.read(buffer, 0, (int) count);
				else
					i = in.read(buffer, 0, bufferSize);
				if (i == -1)
					break;
				count -= i;
				out.write(buffer, 0, i);
			}
		} else {
			while (true) {
				i = in.read(buffer, 0, bufferSize);
				if (i < 0)
					break;
				out.write(buffer, 0, i);
			}
		}
	}
}
