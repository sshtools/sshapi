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
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.sftp.SftpClient;

class SFTPPutTest extends AbstractConnectionTest {

	static {
		try {
			Util.createTempFile();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
//		Native.setProtected(true);
	}

	public SFTPPutTest() throws IOException {
		super();
		configuration.addRequiredCapability(Capability.SFTP);
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		try (SftpClient sftp = client.sftp()) {
			for (int i = 0; i < iterations; i++) {
				try (FileInputStream fin = new FileInputStream(Util.TEST_FILE)) {
					sftp.put(Util.TEST_FILE.getName(), fin, 0644);
				}
			}
		}
	}

	public static void main(String[] arg) throws Exception {
		SFTPPutTest t = new SFTPPutTest();
		t.start();

	}
}
