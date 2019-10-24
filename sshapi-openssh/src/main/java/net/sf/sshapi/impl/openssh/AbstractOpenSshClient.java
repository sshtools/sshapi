package net.sf.sshapi.impl.openssh;

import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.util.Util;

public interface AbstractOpenSshClient {

	default void pump(InputStream in, InputStream errIn) throws IOException {
		Thread readOutput = new Thread("ReadSftpStdoout") {
			@Override
			public void run() {
				try {
					Util.joinStreams(errIn, System.err);
				} catch (Exception ioe) {
				}
			}
		};
		readOutput.start();
		Util.joinStreams(in, System.out);
	}
}
