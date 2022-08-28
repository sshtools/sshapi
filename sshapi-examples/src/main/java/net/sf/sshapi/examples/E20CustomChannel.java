package net.sf.sshapi.examples;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultChannelData;
import net.sf.sshapi.SshChannelHandler;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshCustomChannel;
import net.sf.sshapi.SshCustomChannel.ChannelData;
import net.sf.sshapi.SshCustomChannelListener;
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
		// Basic configuration with a console key validator and console banner handler
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		config.addRequiredCapability(Capability.CHANNEL_HANDLERS);

		// Also display banner messages
		config.setBannerHandler(new ConsoleBannerHandler());

		// Prompt for the host, username and custom channel name

		String connectionSpec = Util.promptConnectionSpec();

		final String channelName = Util.prompt("Enter channel name", "custom@mycompany.com");

		// Create the client using that configuration and connect and authenticate
		try (SshClient client = config.open(connectionSpec, new ConsolePasswordAuthenticator())) {
			ExampleUtilities.dumpClientInfo(client);

			/*
			 * Add a channel handler. When the server requests to open the channel, first
			 * the createChannel() method is called. This allows the client to react to the
			 * initial request data and configure window sizes.
			 * 
			 * Next, channelCreated() will be called which will provide the SshCustomChannel
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

				public void channelCreated(final SshCustomChannel channel) throws IOException {
					System.out.println("Channel " + channel.getName() + " created");
					channel.addListener(new SshCustomChannelListener() {

						public void opened(SshCustomChannel channel) {
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

						public void closing(SshCustomChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}

						public void closed(SshCustomChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}

						public boolean request(SshCustomChannel channel, String requestType, boolean wantReply,
								byte[] requestData) {
							System.out.println("Channel " + channel.getName() + " requests " + requestType
									+ " wants reply = " + wantReply);
							if (requestData != null) {
								System.out.println("Request data: " + new String(requestData));
							}
							return false;
						}

						public void eof(SshCustomChannel channel) {
							System.out.println("Channel " + channel.getName() + " closed");
						}
					});
				}
			});
		}
	}
}
