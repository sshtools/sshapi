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
		File pemFile = new File(Util.prompt("Private key file", System.getProperty("user.home") + File.separator + ".ssh"
			+ File.separator + "id_rsa"));
		FileInputStream in = new FileInputStream(pemFile);
		try {
			SshPrivateKeyFile pk = mgr.createPrivateKeyFromStream(in);

			// Before we can do anything with the key, we must decrypt it if it
			// use encrypted
			if (pk.isEncrypted()) {
				String pw = Util.prompt("Old passphrase");
				pk.decrypt(pw.toCharArray());
			}
			System.out.println(new String(pk.getFormattedKey()));

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
			FileOutputStream fout = new FileOutputStream(pemFile);
			try {
				fout.write(pk.getFormattedKey());
				fout.flush();
			} finally {
				fout.close();
			}

		} finally {
			in.close();
		}

	}
}
