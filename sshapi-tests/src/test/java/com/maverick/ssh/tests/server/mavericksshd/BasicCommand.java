package com.maverick.ssh.tests.server.mavericksshd;

import java.util.Map;

import com.maverick.sshd.platform.ExecutableCommand;

public class BasicCommand extends ExecutableCommand {

	public BasicCommand() {
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
		session.close();
	}

}
