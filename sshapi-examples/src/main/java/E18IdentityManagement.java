import java.io.IOException;
import java.util.StringTokenizer;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshKeyPair;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates listing and changing known hosts.
 * <p>
 * Note, not all implementations identity management
 * 
 */
public class E18IdentityManagement {
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
		//
		System.out.println("generate <type> <bits> - generate key. type may be one of " + mgr.getSupportedKeyTypes()
			+ ", size may be one of " + mgr.getSupportedKeyLengths() + ", format may be one of "
			+ mgr.getSupportedPublicKeyFileFormats());
		System.out
			.println("write <public-format> <private-format> <passphrase> <comment> - write out key. public format may be one of "
				+ mgr.getSupportedPublicKeyFileFormats() + ", private format may be one of "
				+ mgr.getSupportedPrivateKeyFileFormats());
		System.out.println("exit - quit this utility");
		String cmd = "";
		SshKeyPair keyPair = null;
		while (true) {
			cmd = Util.prompt("Command: ");
			if (cmd.startsWith("generate ")) {
				keyPair = generate(mgr, cmd);
			} else if (cmd.startsWith("write")) {
				write(mgr, cmd, keyPair);
			} else if (cmd.equals("get")) {
				break;
			} else if (cmd.equals("exit")) {
				break;
			} else {
				System.out.println("Invalid command");
			}
		}
	}

	protected static void write(SshIdentityManager mgr, String cmd, SshKeyPair keyPair) throws SshException, IOException {
		if (keyPair == null) {
			System.err.println("No key pair in memory. Use generate or load to get one.");
		} else {
			StringTokenizer t = new StringTokenizer(cmd);
			t.nextToken();
			int publicFormat = Integer.parseInt(t.nextToken());
			int privateFormat = Integer.parseInt(t.nextToken());
			String passphrase = t.nextToken();
			char[] pass = Util.nullOrTrimmedBlank(passphrase) ? null : passphrase.toCharArray();
			String comment = t.nextToken();
			if (Util.nullOrTrimmedBlank(comment)) {
				comment = "E18IdentityManagment example, part of SSHAPI";
			}
			SshPublicKeyFile pubKeyFile = mgr.create(keyPair.getPublicKey(), null, comment, publicFormat);
			SshPrivateKeyFile privKeyFile = mgr.create(keyPair, privateFormat, pass, comment);
			String formatted = new String(pubKeyFile.getFormattedKey());
			System.out.println("Public Key:" + formatted);
			System.out.println("Private Key:" + new String(privKeyFile.getFormattedKey()));
		}
	}

	protected static SshKeyPair generate(SshIdentityManager mgr, String cmd) throws SshException {
		SshKeyPair keyPair;
		StringTokenizer t = new StringTokenizer(cmd);
		t.nextToken();
		String type = t.nextToken();
		int bits = Integer.parseInt(t.nextToken());
		keyPair = mgr.generateKeyPair(type, bits);
		if (keyPair == null) {
			System.out.println("No key pair generated");
		} else {
			SshPublicKey publicKey = keyPair.getPublicKey();
			System.out.println("Generated " + publicKey.getFingerprint() + " using " + publicKey.getAlgorithm() + " with "
				+ publicKey.getBitLength());
		}
		return keyPair;
	}
}
