
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sshapi.SshConfiguration;

class Util {
	public static File TEST_FILE = new File("target/test-file");

	protected static void createTempFile() throws FileNotFoundException, IOException {
		long requiredFileSize = Long.parseLong(AbstractConnectionTest.PROPERTIES.getProperty("fileSize", "65536"));
		if (requiredFileSize != TEST_FILE.length()) {
			SshConfiguration.getLogger().info("Generating test file {0} of {1} bytes", TEST_FILE, requiredFileSize);
			FileOutputStream fos = new FileOutputStream(TEST_FILE);
			try {
				for (int i = 0; i < requiredFileSize; i++) {
					fos.write((int) (Math.random() * 256));
				}
			} finally {
				fos.close();
			}

			SshConfiguration.getLogger().info("Generated test file {0}", TEST_FILE);
		}
	}

}
