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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates changing the passphrase of a private key using the
 * {@link SshIdentityManager}.
 * <p>
 * Note, not all implementations support identity management
 * 
 */
public class E12ChangeKeyPassphrase {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.IDENTITY_MANAGEMENT);
		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Got provider " + provider.getClass());
		SshIdentityManager mgr = provider.createIdentityManager(config);
		
		// Private key
		File pemFile = new File(Util.prompt("Private key file",
				System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"));
		SshPrivateKeyFile pk;
		try (FileInputStream in = new FileInputStream(pemFile)) {
			pk = mgr.createPrivateKeyFromStream(in);
			
			// Before we can do anything with the key, we must decrypt it if it
			// use encrypted
			if (pk.isEncrypted()) {
				String pw = Util.prompt("Old passphrase");
				pk.decrypt(pw.toCharArray());
			}
			System.out.println(new String(pk.getFormattedKey()));
		}
		
		// Change
		int i = 0;
		String newpw = null;
		for (; i < 2; i++) {
			newpw = Util.prompt("New passphrase");
			String confirmpw = Util.prompt("Confirm new passphrase");
			if (newpw.equals(confirmpw)) {
				break;
			} else {
				System.out.println("Passphrases do not match");
			}
		}
		if (i == 2) {
			System.out.println("Aborted Exit");
			System.exit(0);
		}
		pk.changePassphrase(newpw.toCharArray());
		
		// Write the key back out
		try (FileOutputStream fout = new FileOutputStream(pemFile)) {
			fout.write(pk.getFormattedKey());
		}
	}
}
