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
import java.io.File;
import java.io.IOException;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshSCPClient;

class SCPGetTest extends AbstractConnectionTest {
	public SCPGetTest() throws IOException {
		super();
		configuration.addRequiredCapability(Capability.SCP);
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		File tempFile = File.createTempFile("scp", "tst");
		try (SshSCPClient scp = client.scp()) {
			for (int i = 0; i < iterations; i++) {
				scp.get("test-file", tempFile, false);
			}
		} finally {
			tempFile.delete();
		}
	}

	public static void main(String[] arg) throws Exception {
		SCPGetTest t = new SCPGetTest();
		t.start();
	}
}
