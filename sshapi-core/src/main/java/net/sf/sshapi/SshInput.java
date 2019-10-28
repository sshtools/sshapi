package net.sf.sshapi;

import java.nio.ByteBuffer;

import net.sf.sshapi.Logger.Level;

public interface SshInput {
	void read(ByteBuffer buffer);
	
	default void onError(Exception e) {
		SshConfiguration.getLogger().log(Level.ERROR, "Error reading input.", e);
	}
}
