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
