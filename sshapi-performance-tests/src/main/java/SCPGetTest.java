import java.io.File;
import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshSCPClient;

class SCPGetTest extends AbstractConnectionTest {

	public SCPGetTest() throws IOException {
		super();
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		SshSCPClient scp = client.createSCPClient();
		scp.open();
		File tempFile = File.createTempFile("scp", "tst");
		try {
			try {
				scp.get("test-file", tempFile, false);
			} finally {
				scp.close();
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
