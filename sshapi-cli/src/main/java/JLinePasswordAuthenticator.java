import java.io.IOException;

import jline.ConsoleReader;
import jline.Terminal;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.auth.SshPasswordAuthenticator;

/**
 * {@link SshPasswordAuthenticator} that uses JLine to read a password. This means the
 * password echo can be masked.
 */
public class JLinePasswordAuthenticator implements SshPasswordAuthenticator {

	private Terminal terminal;
	private ConsoleReader reader;

	/**
	 * COnstructor.
	 * 
	 * @param terminal terminal
	 * @param reader reader
	 */
	public JLinePasswordAuthenticator(Terminal terminal, ConsoleReader reader) {
		this.terminal = terminal;
		this.reader = reader;
	}

	public char[] promptForPassword(SshClient session, String message) {
		try {
			String password = reader.readLine(message + (message.endsWith(":") ? " " : ": "), Character.valueOf('*'));
			return password.equals("") ? null : password.toCharArray();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public String getTypeName() {
		return "password";
	}
}