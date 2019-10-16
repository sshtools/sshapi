import java.io.IOException;

import net.sf.sshapi.Capability;
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
		configuration.addRequiredCapability(Capability.SCP);
	}

	protected void doConnection(SshClient client) throws Exception {
		super.doConnection(client);
		try (SshSCPClient scp = client.scp()) {
			for (int i = 0; i < iterations; i++) {
				scp.put("test-file", null, Util.TEST_FILE, false);
			}
		}
	}

	public static void main(String[] arg) throws Exception {
		SCPPutTest t = new SCPPutTest();
		t.start();
	}
}
