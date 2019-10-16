package net.sf.sshapi.cli;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.auth.SshPasswordAuthenticator;

/**
 * {@link SshPasswordAuthenticator} that uses JLine to read a password. This
 * means the password echo can be masked.
 */
public class JLinePasswordAuthenticator implements SshPasswordAuthenticator {

	private Terminal terminal;
	private LineReader reader;

	/**
	 * COnstructor.
	 * 
	 * @param terminal terminal
	 * @param reader   reader
	 */
	public JLinePasswordAuthenticator(Terminal terminal, LineReader reader) {
		this.terminal = terminal;
		this.reader = reader;
	}

	public char[] promptForPassword(SshClient session, String message) {
		String password = reader.readLine(message + (message.endsWith(":") ? " " : ": "), Character.valueOf('*'));
		return password.equals("") ? null : password.toCharArray();
	}

	public String getTypeName() {
		return "password";
	}
}