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
package com.maverick.ssh.tests.server.synergysshd;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.sshtools.common.command.AbstractExecutableCommand;

public class CommandWithOutput extends AbstractExecutableCommand {
	
	public static class CommandWithOutputFactory implements ExecutableCommandFactory<CommandWithOutput> {
		@Override
		public CommandWithOutput create() throws NoSuchAlgorithmException, IOException {
			return new CommandWithOutput();
		}

		@Override
		public String[] getKeys() {
			return new String[] { "commandWithOutput" };
		}
	}

	public CommandWithOutput() {
		super();
	}

	public int getExitCode() {
		return 0;
	}

	public void kill() {
	}

	public boolean createProcess(String[] args, Map<String,String> environment) {
		return true;
	}

	public void onStart() {
		try {
			getOutputStream().write("This is line 1\r\nThis is line 2\r\nThis is line 3".getBytes());
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}

}
