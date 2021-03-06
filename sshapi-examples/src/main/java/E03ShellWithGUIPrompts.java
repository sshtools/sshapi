import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.sshapi.SshBannerHandler;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.Util;

/**
 * This examples extends the {@link E01Shell} example, except host user name and
 * password are all prompted for using Swing components. This demonstrates the
 * user of a custom {@link SshAuthenticator} as well as a custom
 * {@link HostKeyValidator} and {@link SshBannerHandler}
 */
public class E03ShellWithGUIPrompts {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Create a new configuration so you can set the host key verifier
		SshConfiguration config = new SshConfiguration();
		config.setHostKeyValidator(new HostKeyValidator());
		config.setBannerHandler(new BannerHandler());

		// Create the client using that configuration
		SshClient client = config.createClient();
		ExampleUtilities.dumpClientInfo(client);

		// Prompt for the host and username
		String connectionSpec = JOptionPane.showInputDialog("Enter username@hostname", System.getProperty("user.name") + "@localhost");
		if (connectionSpec == null) {
			return;
		}
		String host = ExampleUtilities.extractHostname(connectionSpec);
		String user = ExampleUtilities.extractUsername(connectionSpec);
		int port = ExampleUtilities.extractPort(connectionSpec);

		// Connect, authenticate
		client.connect(user, host, port);
		client.authenticate(new ShellAuthenticator());

		try {
			// Start the shell
			SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
			try {
				shell.open();
				ExampleUtilities.joinShellToConsole(shell);
			} finally {
				shell.close();
			}
		} finally {
			client.disconnect();
		}
	}

	static class HostKeyValidator implements SshHostKeyValidator {
		public int verifyHost(SshHostKey hostKey) throws SshException {
			String message = "The authenticity of host '" + hostKey.getHost() + "' can't be established.\n";
			String keyAlgorithm = hostKey.getType();
			String hexFingerprint = hostKey.getFingerprint();
			message += (keyAlgorithm == null ? "?" : keyAlgorithm) + " key fingerprint is "
				+ (hexFingerprint == null ? "unknown" : hexFingerprint);
			int result = JOptionPane.showConfirmDialog(null, message, "Host Key Verification", JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				return SshHostKeyValidator.STATUS_HOST_KEY_VALID;
			}
			return SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
		}
	}

	static class BannerHandler implements SshBannerHandler {
		public void banner(String message) {
			JOptionPane.showMessageDialog(null, message);
		}
	}

	static class ShellAuthenticator implements SshPasswordAuthenticator {

		public char[] promptForPassword(SshClient session, String message) {
			final JTextField passwordField = (JTextField) new JPasswordField(20);
			Object[] ob = { passwordField };
			int result = JOptionPane.showConfirmDialog(null, ob, message, JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				return passwordField.getText().toCharArray();
			} else {
				return null;
			}
		}

		public String getTypeName() {
			return "password";
		}
	}
}
