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
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Some servers support remote management of authorized keys. This will also
 * require such support in the provider.
 */
public class E16PublicKeySubsytem {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.PUBLIC_KEY_SUBSYSTEM);
		config.setHostKeyValidator(new ConsoleHostKeyValidator());
		config.setBannerHandler(new ConsoleBannerHandler());

		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		SshClient client = provider.createClient(config);
		ExampleUtilities.dumpClientInfo(client);

		client.connect(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator());
		System.out.println("Remote identification: " + client.getRemoteIdentification());

		//
		System.out.println("list - list all keys");
		String cmd = "";

		try (SshPublicKeySubsystem subsys = client.publicKeySubsystem()) {
			while (true) {
				cmd = Util.prompt("Command: ");
				if (cmd.equals("list")) {
					SshPublicKey[] keys = subsys.list();
					if (keys == null) {
						System.out.println("No keys");
					} else {
						for (int i = 0; i < keys.length; i++) {
							System.out.println(printKey(keys[i]));
						}
					}
				} else {
					System.out.println("Invalid command");
				}
			}
		}
	}

	private static String printKey(SshPublicKey sshHostKey) throws SshException {
		return sshHostKey.getAlgorithm() + " " + sshHostKey.getBitLength() + " " + sshHostKey.getFingerprint();
	}
}
