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
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates local port forwarding.
 */
public class E06bLocalForwardingAndShell {
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
//		config.addRequiredCapability(Capability.PORT_FORWARD_EVENTS);

		// Create the client using that configuration.
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			ExampleUtilities.dumpClientInfo(client);

			// If our provider supports it, adds listen for the events as tunneled
			// connections become active
			if (client.getProvider().getCapabilities().contains(Capability.PORT_FORWARD_EVENTS)) {
				client.addPortForwardListener(new SshPortForwardListener() {

					public void channelOpened(int type, SshPortForwardTunnel channel) {
						System.out.println("Channel open: " + type + " / " + channel);

					}

					public void channelClosed(int type, SshPortForwardTunnel channel) {
						System.out.println("Channel closed: " + type + " / " + channel);
					}
				});
			}

			try (SshPortForward local = client.localForward(null, 8443, "tools.ietf.org", 443)) {

				System.out.println("Point your browser to https://localhost:8443/");
				
				// Create the shell channel, tunnel will be active until that exits
				try (SshShell shell = client.shell("dumb", 80, 24, 0, 0, null)) {

					/*
					 * Call the utility method to join the remote streams to the console streams
					 */
					ExampleUtilities.joinShellToConsole(shell);
				}
			}
		}

	}
}
