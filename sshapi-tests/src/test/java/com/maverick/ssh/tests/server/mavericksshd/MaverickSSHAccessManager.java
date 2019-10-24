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
