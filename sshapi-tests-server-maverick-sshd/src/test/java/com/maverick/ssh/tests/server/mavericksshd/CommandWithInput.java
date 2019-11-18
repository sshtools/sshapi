package com.maverick.ssh.tests.server.mavericksshd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.maverick.sshd.platform.ExecutableCommand;

public class CommandWithInput extends ExecutableCommand {

	public CommandWithInput() {
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
		new Thread("CommandWithInput") {
			public void run() {
				try {
					List<String> l = new ArrayList<String>();
					int line;
					StringBuilder bui = new StringBuilder();
					while ((line = (char) stdin.read()) != -1) {
						System.out.println(String.format("%04d - %02x - %s", line, line, String.valueOf((char)line)));
						if ( ( line == 65535 || line == 4 ) && bui.length() == 0) {
							break;
						}
						if (line == '\n') {
							l.add(bui.toString());
							bui.setLength(0);
						} else {
							bui.append((char) line);
						}
					}
					Collections.reverse(l);
					for (String s : l) {
						stdout.write((s + "\r\n").getBytes());
					}
					stdout.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					session.close();
				}
			}
		}.start();
	}

}
