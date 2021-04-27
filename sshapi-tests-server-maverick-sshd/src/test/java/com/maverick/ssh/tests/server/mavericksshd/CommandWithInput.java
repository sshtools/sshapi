/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
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
