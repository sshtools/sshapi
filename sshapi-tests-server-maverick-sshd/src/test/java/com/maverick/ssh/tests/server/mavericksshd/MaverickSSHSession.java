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

/* HEADER */
import java.io.IOException;

import com.maverick.sshd.Connection;
import com.maverick.sshd.ConnectionManager;
import com.maverick.sshd.SessionChannel;
import com.maverick.sshd.scp.ScpCommand;

public class MaverickSSHSession extends SessionChannel {

	String prompt = "[foo@maverick] ";
	String input = "";
	ScpCommand scp;

	public MaverickSSHSession() {
		super();
	}

	protected void processStdinData(byte[] data) {

		if (scp != null) {
			try {
				scp.getOutputStream().write(data);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			boolean newline = false;
			for (int pos = 0; pos < data.length; pos++) {

				if (data[pos] == '\n' || data[pos] == '\r' && !newline) {
					input += "\r\n";
					sendChannelData("\r\n".getBytes());
					sendChannelData(input.getBytes());
					if (input.trim().equalsIgnoreCase("exit")) {
						close();
						connection.disconnect();
					} else
						sendChannelData(prompt.getBytes());
					input = "";
					newline = true;
				} else {
					sendChannelData(data, pos, 1);
					input += (char) data[pos];
					newline = false;
				}
			}
		}

	}

	protected void processStderrData(byte[] data) {

	}

	protected void onChannelClosed() {

	}

	protected boolean executeCommand(String cmd) {
		return false;
	}

	protected void changeWindowDimensions(int cols, int rows, int width,
			int height) {

	}

	protected void onSessionOpen() {

		if (scp == null) {
			prompt = "[" + connection.getUsername() + "@maverick] ";
			String str = "Maverick SSHD\r\n(c) 2003-2009 SSHTools Ltd. All Rights Reserved\r\n\r\nThis shell will echo all input.\r\n";
			sendChannelData(str.getBytes());

			str = "Your logged on from "
					+ ((Connection) ConnectionManager.getInstance()
							.getConnectionById(getSessionIdentifier()))
							.getRemoteAddress() + "\r\n";
			sendChannelData(str.getBytes());

			sendStderrData("This is STDERR data\r\n".getBytes());
			sendChannelData(prompt.getBytes());
		}
	}

	protected void onLocalEOF() {
		// The local side is EOF no more data can be sent

	}

	protected boolean startShell() {
		return true;
	}

	protected boolean allocatePseudoTerminal(String term, int cols, int rows,
			int width, int height, byte[] encoded) {
		return true;
	}

	protected void processSignal(String signal) {

	}

	protected boolean setEnvironmentVariable(String name, String value) {
		return true;
	}
}
