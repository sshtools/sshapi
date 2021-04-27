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
import java.nio.ByteBuffer;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpClient.OpenMode;
import net.sf.sshapi.sftp.SftpHandle;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Demonstrates opening files directory to read or write instead of using the
 * {@link SftpClient#get(String)} or {@link SftpClient#put(String, int)}
 * variants.
 */
public class E22RawSFTP {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// Configure
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		config.addRequiredCapability(Capability.RAW_SFTP);
		
		// Connect and authenticate
		try (SshClient client = config.open(Util.promptConnectionSpec(), new ConsolePasswordAuthenticator())) {
			try (SftpClient sftp = client.sftp()) {
				// Write test file
				try (SftpHandle handle = sftp.file("test-file.txt", OpenMode.SFTP_WRITE, OpenMode.SFTP_CREAT)) {
					ByteBuffer buf = ByteBuffer.allocate(16);
					buf.putInt(1);
					buf.putInt(2);
					buf.putInt(3);
					buf.putInt(4);
					buf.flip();
					handle.write(buf);
				}
				
				// Read 4 bytes from test file from 4th byte, which is the 2nd 'int' written above
				try (SftpHandle handle = sftp.file("test-file.txt", OpenMode.SFTP_READ)) {
					ByteBuffer buf = ByteBuffer.allocate(4);
					handle.position(4).read(buf);
					if(buf.getInt(0) != 2) {
						throw new IllegalStateException("Expected to receive value of 2.");
					}
				}
			}
		}
	}
}
