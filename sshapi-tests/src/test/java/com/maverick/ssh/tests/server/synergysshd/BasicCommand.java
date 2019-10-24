package com.maverick.ssh.tests.server.synergysshd;

import java.io.IOException;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class BasicCommand extends ShellCommand {
	public BasicCommand() {
		super("basicCommand", "test", "test", "test");
	}

	@Override
	public void run(String[] args, VirtualConsole console) throws IOException, PermissionDeniedException, UsageException {
		console.destroy();
	}
}
