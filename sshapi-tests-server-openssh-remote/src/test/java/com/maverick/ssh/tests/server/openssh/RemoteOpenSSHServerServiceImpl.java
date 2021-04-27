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
