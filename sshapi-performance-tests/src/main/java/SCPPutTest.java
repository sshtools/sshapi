import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshSCPClient;

class SCPPutTest extends AbstractConnectionTest {

	static {
		try {
			Util.createTempFile();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	public SCPPutTest() throws IOException {
		super();
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		SshSCPClient scp = client.createSCPClient();
		scp.open();
		try {
			scp.put("test-file", null, Util.TEST_FILE, false);
		} finally {
			scp.close();
		}
	}

	public static void main(String[] arg) throws Exception {
		SCPPutTest t = new SCPPutTest();
		t.start();

	}
}
