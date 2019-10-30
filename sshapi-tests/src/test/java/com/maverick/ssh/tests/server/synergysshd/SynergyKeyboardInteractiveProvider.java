package com.maverick.ssh.tests.server.synergysshd;

import java.io.IOException;
import java.util.Collection;

import com.sshtools.common.auth.KeyboardInteractiveAuthenticationProvider;
import com.sshtools.common.auth.KeyboardInteractiveProvider;
import com.sshtools.common.auth.PasswordKeyboardInteractiveProvider;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.common.ssh2.KBIPrompt;

/**
 * This is effectively a copy of {@link PasswordKeyboardInteractiveProvider},
 * but it will stop Maverick SSHD treating KBI the same as password.
 */
public class SynergyKeyboardInteractiveProvider implements KeyboardInteractiveAuthenticationProvider {
	final static int REQUESTED_PASSWORD = 1;
	final static int CHANGING_PASSWORD = 2;
	final static int FINISHED = 2;

	@Override
	public KeyboardInteractiveProvider createInstance(SshConnection con) throws IOException {
		return new KeyboardInteractiveProvider() {
			private String password;
			private boolean success = false;
			private String name = "password";
			private String instruction = "";
			private int state = REQUESTED_PASSWORD;
			private int maxAttempts = 2;
			private SshConnection connection;

			@Override
			public boolean hasAuthenticated() {
				return success;
			}

			@Override
			public boolean setResponse(String[] answers, Collection<KBIPrompt> additionalPrompts) throws IOException {
				if (answers.length == 0) {
					throw new RuntimeException("Not enough answers!");
				}
				switch (state) {
				case REQUESTED_PASSWORD:
					password = answers[0];
//					try {
						// TODO 
						success = true;
//						success = auth.getProvider().verifyPassword(
//								connection, connection.getUsername(),
//								password, auth.getTransport().getRemoteAddress());
						state = FINISHED;
						return true;
//					} 
//					catch (PasswordChangeException e) {
//						state = CHANGING_PASSWORD;
//						KBIPrompt[] prompts = new KBIPrompt[2];
//						additionalPrompts.add(new KBIPrompt("New Password:", false));
//						additionalPrompts.add(new KBIPrompt("Confirm Password:", false));
//						if (e.getMessage() == null)
//							instruction = "Enter new password for " + connection.getUsername();
//						else
//							instruction = e.getMessage();
//						return false;
//					}
				case CHANGING_PASSWORD:
					if (answers.length < 2) {
						throw new RuntimeException("Not enough answers!");
					}
					if (maxAttempts <= 0) {
						state = FINISHED;
						return true;
					}
					String password1 = answers[0];
					String password2 = answers[1];
					if (password1.equals(password2)) {
						success = true;
//						success = auth.getProvider().changePassword(
//								ConnectionManager.getInstance().getConnectionById(auth.getTransport().getUUID()), connection.getUsername(),
//								password, password1);
						if (success) {
							state = FINISHED;
							return true;
						}
						state = CHANGING_PASSWORD;
						additionalPrompts.add(new KBIPrompt("New Password:", false));
						additionalPrompts.add(new KBIPrompt("Confirm Password:", false));
						instruction = "Password change failed! Enter new password for " + connection.getUsername();
						maxAttempts--;
						return false;
					} else {
						instruction = "Passwords do not match! Enter new password for " + connection.getUsername();
						additionalPrompts.add(new KBIPrompt("New Password:", false));
						additionalPrompts.add(new KBIPrompt("Confirm Password:", false));
						maxAttempts--;
						return false;
					}
				default:
					throw new RuntimeException("We shouldn't be here");
				}
			}

			@Override
			public KBIPrompt[] init(SshConnection connection) {
				this.connection =  connection;
				KBIPrompt[] prompts = new KBIPrompt[1];
				prompts[0] = new KBIPrompt("Password:", false);
				instruction = "Enter password for " + connection.getUsername();
				return prompts;
			}

			@Override
			public String getInstruction() {
				return instruction;
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}
}