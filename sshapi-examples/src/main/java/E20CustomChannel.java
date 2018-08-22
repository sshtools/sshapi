import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultChannelData;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshChannel.ChannelData;
import net.sf.sshapi.SshChannelHandler;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Waits for a custom channel to be opened, sends a reply and closes the
 * connection.
 * <p>
 * <strong>Note, this will require a server that can open a custom channel and
 * send a request to it</strong>.
 * 
 */
public class E20CustomChannel {

	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.CHANNEL_HANDLERS);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());

		// Also display banner messages
		config.setBannerHandler(new ConsoleBannerHandler());

		// Prompt for the host, username and custom channel name

		String connectionSpec = Util.prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		final String channelName = Util.prompt("Enter channel name", "custom@mycompany.com");

		// Create the client using that configuration and connect and authenticate
		try (SshClient client = config.open(user, host, port, new ConsolePasswordAuthenticator())) {
			ExampleUtilities.dumpClientInfo(client);

			/*
			 * Add a channel handler. When the server requests to open the channel, first
			 * the createChannel() method is called. This allows the client to react to the
			 * initial request data and configure window sizes.
			 * 
			 * Next, channelCreated() will be called which will provide the SshChannel
			 * object. This may then have event listeners attached to it to react on
			 * opening, closing and other events.
			 */

			client.addChannelHandler(new SshChannelHandler() {

				public String[] getSupportChannelNames() {
					return new String[] { channelName };
				}

				public ChannelData createChannel(String channelName, byte[] requestData) {
					return new DefaultChannelData(32768, 32768, 0, requestData);
				}

				public void channelCreated(final SshChannel channel) throws IOException {
					System.out.println("Channel " + channel.getName() + " created");
					channel.addListener(new SshChannelListener<SshChannel>() {

						public void opened(SshChannel channel) {
							System.out.println("Channel " + channel.getName() + " opened");
							byte[] rd = channel.getChannelData().getRequestData();
							if (rd != null) {
								System.out.println("Data: " + new String(rd));
							}
							try {
								final InputStream in = channel.getInputStream();
								final OutputStream out = channel.getOutputStream();
								new Thread() {
									public void run() {
										try {
											ExampleUtilities.copy(in, System.out);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									}
								}.start();
								new Thread() {
									public void run() {
										try {
											ExampleUtilities.copy(System.in, out);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									}
								}.start();
							} catch (IOException ioe) {
								throw new RuntimeException(ioe);
							}
						}

						public void closing(SshChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}

						public void closed(SshChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}

						public boolean request(SshChannel channel, String requestType, boolean wantReply,
								byte[] requestData) {
							System.out.println("Channel " + channel.getName() + " requests " + requestType
									+ " wants reply = " + wantReply);
							if (requestData != null) {
								System.out.println("Request data: " + new String(requestData));
							}
							return false;
						}

						public void eof(SshChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}
					});
				}
			});
		}
	}
}
