
import java.io.File;
import java.io.IOException;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.sftp.SftpClient;

class SFTPGetTest extends AbstractConnectionTest {

	public SFTPGetTest() throws IOException {
		super();
		configuration.addRequiredCapability(Capability.SFTP);
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		File tempFile = File.createTempFile("sftp", "tst");
		try (SftpClient sftp = client.sftp()) {
			for (int i = 0; i < iterations; i++) {
				sftp.get("test-file", tempFile);
			}
		} finally {
			tempFile.delete();
		}
	}

	public static void main(String[] arg) throws Exception {
		SFTPGetTest t = new SFTPGetTest();
		t.start();

	}
}
