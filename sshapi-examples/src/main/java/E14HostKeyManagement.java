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
import java.util.StringTokenizer;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.hostkeys.SshManagedHostKey;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates listing and changing known hosts.
 * <p>
 * Note, not all implementations support host key management
 * 
 */
public class E14HostKeyManagement {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.HOST_KEY_MANAGEMENT);

		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Got provider " + provider.getClass());
		SshHostKeyManager mgr = provider.createHostKeyManager(config);

		/*
		 * Configure a host key validator. Pass the host key manager so the
		 * validator can add new host keys and check for mismatches against
		 * stored ones
		 */
		config.setHostKeyValidator(new ConsoleHostKeyValidator(mgr));

		//
		System.out.println("list - list all keys");
		System.out.println("exit - quit this utility");
		System.out.println("get <hostname> [<type>] - get all keys for the host (and optional type)");
		System.out.println("remove <type> <hostname> - remove all keys of specified type and host");
		String cmd = "";
		while (true) {
			cmd = Util.prompt("Command: ");
			if (cmd.equals("list")) {
				SshHostKey[] keys = mgr.getKeys();
				if (keys == null) {
					System.out.println("No keys");
				} else {
					for (int i = 0; i < keys.length; i++) {
						System.out.println(printKey(keys[i]));
					}
				}
			} else if (cmd.startsWith("get ")) {
				StringTokenizer t = new StringTokenizer(cmd);
				t.nextToken();
				String host = t.nextToken();
				String type = t.hasMoreTokens() ? t.nextToken() : null;
				SshHostKey[] keys = mgr.getKeysForHost(host, type);
				if (keys == null) {
					System.out.println("No keys for this host");
				} else {
					for (int i = 0; i < keys.length; i++) {
						System.out.println(printKey(keys[i]));
					}
				}
			} else if (cmd.startsWith("remove ")) {
				StringTokenizer t = new StringTokenizer(cmd);
				t.nextToken();
				String type = t.nextToken();
				String host = t.nextToken();
				SshManagedHostKey[] keys = mgr.getKeysForHost(host, type);
				if (keys == null) {
					System.out.println("No keys for this host");
				} else {
					for (int i = 0; i < keys.length; i++) {
						mgr.remove(keys[i]);
						System.out.println("Removed key " + printKey(keys[i]));
					}
				}
			} else if (cmd.equals("exit")) {
				break;
			} else {
				System.out.println("Invalid command");
			}
		}
	}

	private static String printKey(SshHostKey sshHostKey) {
		return sshHostKey.getType() + " " + sshHostKey.getHost() + " " + sshHostKey.getFingerprint();
	}
}
