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

import java.net.SocketAddress;

import com.maverick.sshd.AccessManager;
import com.maverick.sshd.Channel;
import com.maverick.sshd.ForwardingChannel;

public class MaverickSSHAccessManager implements AccessManager {

	public boolean canConnect(String username) {
		return true;
	}

	public boolean canConnect(SocketAddress remoteclient, SocketAddress localAddress) {
		return true;
	}

	public boolean canOpenChannel(String sessionid, String username,
			Channel channel) {
		return true;
	}

	public boolean canStartShell(String sessionid, String username) {
		return true;
	}

	public boolean canExecuteCommand(String sessionid, String username,
			String cmd) {
		return true;
	}

	public boolean canStartSubsystem(String sessionid, String username,
			String subsystem) {
		return true;
	}

	public boolean canForward(String sessionid, String username,
			ForwardingChannel channel, boolean isLocal) {
		return true;
	}

	public boolean canListen(String sessionid, String username,
			String bindAddress, int bindPort) {
		return true;
	}

	public String[] getRequiredAuthentications(String sessionid, String username) {
		return null;
	}
}
