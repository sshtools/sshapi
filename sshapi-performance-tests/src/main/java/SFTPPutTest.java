
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
