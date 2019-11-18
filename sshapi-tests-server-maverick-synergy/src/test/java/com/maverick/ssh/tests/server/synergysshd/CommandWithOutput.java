package com.maverick.ssh.tests.server.synergysshd;

import java.io.IOException;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class CommandWithOutput extends ShellCommand {

	public CommandWithOutput() {
		super("commandWithOutput", "test", "test", "test");
	}

	@Override
	public void run(String[] args, VirtualConsole console) throws IOException, PermissionDeniedException, UsageException {
		console.println("This is line 1\r\nThis is line 2\r\nThis is line 3");
		console.destroy();
	}

}
