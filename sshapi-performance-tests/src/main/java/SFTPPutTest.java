import java.io.FileInputStream;
import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.sftp.SftpClient;

class SFTPPutTest extends AbstractConnectionTest {

	static {
		try {
			Util.createTempFile();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public SFTPPutTest() throws IOException {
		super();
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		SftpClient sftp = client.createSftpClient();
		sftp.open();
		FileInputStream fin = new FileInputStream(Util.TEST_FILE);
		try {
			sftp.put(Util.TEST_FILE.getName(), fin, 0644);
		} finally {
			fin.close();
		}
	}

	public static void main(String[] arg) throws Exception {
		SFTPPutTest t = new SFTPPutTest();
		t.start();

	}
}
