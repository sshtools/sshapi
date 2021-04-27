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
package com.maverick.ssh.tests.client.tests;

import java.io.IOException;

import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientMultipleConnected;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.sftp.SftpClient;

public class StopStartTest extends AbstractClientMultipleConnected {

	@Override
	protected void onClientSetup() {
		Assume.assumeTrue("Server must be Maverick Synergy", config.getName().endsWith("-synergy-server"));
	}

	@Test
	public void stopStartTest() throws Exception {
		
		for(int i=0;i<10;i++) {
			disconnect();
			server.restart();
			connect();
			
		}
	}
	
	@Override
	protected void onConnectingSetUp() throws net.sf.sshapi.SshException, IOException {
		super.onConnectingSetUp();
		
		for(SshClient ssh : clients) {
			try(SftpClient sftp = ssh.sftp()) {
				sftp.ls(sftp.getDefaultPath());
			}
		}
	}

	@Override
	public int getConnectionCount() {
		return 100;
	}
}
