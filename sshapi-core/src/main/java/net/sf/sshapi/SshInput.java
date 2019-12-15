package net.sf.sshapi;

import java.nio.ByteBuffer;

public interface SshInput {
	void read(ByteBuffer buffer);
	
	default void onError(Exception e) {
		SshConfiguration.getLogger().error("Error reading input.", e);
	}
}
