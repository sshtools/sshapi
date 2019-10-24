package com.maverick.ssh.tests.server.mavericksshd;

import com.maverick.ssh2.KBIPrompt;
import com.maverick.sshd.ConnectionManager;
import com.maverick.sshd.KeyboardInteractiveAuthentication;
import com.maverick.sshd.PasswordKeyboardInteractiveProvider;
import com.maverick.sshd.platform.KeyboardInteractiveProvider;
import com.maverick.sshd.platform.PasswordChangeException;

/**
 * This is effectively a copy of {@link PasswordKeyboardInteractiveProvider}, but it will
 * stop Maverick SSHD treating KBI the same as password.
 */
public class MaverickSSHKeyboardInteractiveProvider implements
		KeyboardInteractiveProvider {

	final static int REQUESTED_PASSWORD = 1;
	final static int CHANGING_PASSWORD = 2;
	final static int FINISHED = 2;

	private String username;
	private String password;
	private KeyboardInteractiveAuthentication auth;
	private boolean success = false;
	private String name = "password";
	private String instruction = "";

	private int state = REQUESTED_PASSWORD;
	private int maxAttempts = 2;

	public boolean hasAuthenticated() {
		return success;
	}

	public KBIPrompt[] setResponse(String[] answers) {

		if (answers.length == 0) {
			throw new RuntimeException("Not enough answers!");
		}

		switch (state) {
		case REQUESTED_PASSWORD:

			password = answers[0];

			try {
				success = auth.getProvider().verifyPassword(ConnectionManager.getInstance().getConnectionById(auth.getTransport().getUUID()), username, password, auth.getTransport().getRemoteAddress());
				
				state = FINISHED;
				return null;
			} catch (PasswordChangeException e) {
				state = CHANGING_PASSWORD;

				KBIPrompt[] prompts = new KBIPrompt[2];
				prompts[0] = new KBIPrompt("New Password:", false);
				prompts[1] = new KBIPrompt("Confirm Password:", false);

				if (e.getMessage() == null)
					instruction = "Enter new password for " + username;
				else
					instruction = e.getMessage();

				return prompts;
			}
		case CHANGING_PASSWORD:
			if (answers.length < 2) {
				throw new RuntimeException("Not enough answers!");
			}

			if (maxAttempts <= 0) {
				state = FINISHED;
				return null;
			}
			String password1 = answers[0];
			String password2 = answers[1];

			if (password1.equals(password2)) {

					success = auth.getProvider().changePassword(ConnectionManager.getInstance().getConnectionById(auth.getTransport().getUUID()),
							username, password, password1);
					
					if(success) {
						state = FINISHED;
						return null;
					}

					state = CHANGING_PASSWORD;

					KBIPrompt[] prompts = new KBIPrompt[2];
					prompts[0] = new KBIPrompt("New Password:", false);
					prompts[1] = new KBIPrompt("Confirm Password:", false);
					instruction = "Password change failed! Enter new password for "
								+ username;

					maxAttempts--;

					return prompts;
			} else {
				KBIPrompt[] prompts = new KBIPrompt[2];
				instruction = "Passwords do not match! Enter new password for "
						+ username;
				prompts[0] = new KBIPrompt("New Password:", false);
				prompts[1] = new KBIPrompt("Confirm Password:", false);

				maxAttempts--;
				return prompts;
			}

		default:
			throw new RuntimeException("We shouldn't be here");
		}

	}

	public KBIPrompt[] init(String username,
			KeyboardInteractiveAuthentication auth) {
		this.username = username;
		this.auth = auth;

		KBIPrompt[] prompts = new KBIPrompt[1];
		prompts[0] = new KBIPrompt("Password:", false);
		instruction = "Enter password for " + username;
		return prompts;
	}

	public String getInstruction() {
		return instruction;
	}

	public String getName() {
		return name;
	}
}
