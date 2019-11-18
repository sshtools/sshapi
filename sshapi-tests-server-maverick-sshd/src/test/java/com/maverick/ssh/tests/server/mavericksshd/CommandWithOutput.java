package com.maverick.ssh.tests.server.mavericksshd;

import java.io.IOException;
import java.util.Map;

import com.maverick.sshd.platform.ExecutableCommand;

public class CommandWithOutput extends ExecutableCommand {

	public CommandWithOutput() {
		super(32768);
	}

	public int getExitCode() {
		return 0;
	}

	public void kill() {
	}

	public boolean createProcess(String cmd, Map<String,String> environment) {
		return true;
	}

	public void onStart() {
		try {
			stdout.write("This is line 1\r\nThis is line 2\r\nThis is line 3".getBytes());
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}

}
