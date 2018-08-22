import java.io.IOException;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;

class ConnectionTest extends AbstractConnectionTest {

	public ConnectionTest() throws IOException {
		super("ConnectionTest");
	}

	protected void doProvider(final SshProvider provider) throws Exception {
		time(provider, new Runnable() {
			public void run() {
				try (SshClient client = connect(provider)) {
				} catch (SshException e) {
					throw new RuntimeException("Failed.", e);
				} catch (IOException e) {
					throw new RuntimeException("Failed.", e);
				}
			}
		});
	}

	protected void doConnection(SshClient client) throws Exception {
		// This just tests connection time so do nothing
	}

	public static void main(String[] arg) throws Exception {
		ConnectionTest t = new ConnectionTest();
		t.start();

	}
}
