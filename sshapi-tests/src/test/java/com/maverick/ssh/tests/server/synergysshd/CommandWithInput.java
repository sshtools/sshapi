package com.maverick.ssh.tests.server.synergysshd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.server.vsession.ShellCommand;
import com.sshtools.server.vsession.UsageException;
import com.sshtools.server.vsession.VirtualConsole;

public class CommandWithInput extends ShellCommand {
	public CommandWithInput() {
		super("commandWithInput", "test", "test", "test");
	}

	@Override
	public void run(String[] args, VirtualConsole console) throws IOException, PermissionDeniedException, UsageException {
		List<String> l = new ArrayList<String>();
		String line;
		while ((line = console.readLine()) != null) {
			l.add(line);
		}
		Collections.reverse(l);
		for (String s : l) {
			console.getTerminal().writer().println(s);
		}
		console.destroy();
	}
}
