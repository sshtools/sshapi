package com.maverick.ssh.tests.server.openssh;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.maverick.ssh.tests.AbstractServer;
import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.SshTestConfiguration;

public class RemoteOpenSSHServerServiceImpl extends AbstractServer {

	public void doStop() throws Exception {
	} 

	public List<ServerCapability> init(SshTestConfiguration configuration,
			Properties serviceProperties) throws Exception {
		if (!configuration.getName().endsWith("openssh-server")) {
			throw new Exception(
					"This server is not intended for use with this configuration.");
		}

		return Arrays
				.asList(ServerCapability.SFTP_LS_RETURNS_DOTS,
						ServerCapability.SUPPORTS_GROUPS,
						ServerCapability.SUPPORTS_PERMISSIONS,
						ServerCapability.SUPPORTS_OWNERS,
						ServerCapability.SYMLINKS);
	}

	@Override
	public void doStart() throws Exception {
	}

}
