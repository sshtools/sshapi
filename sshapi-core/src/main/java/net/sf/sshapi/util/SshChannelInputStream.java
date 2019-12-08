package net.sf.sshapi.util;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.sshapi.SshStreamChannel;

/**
 * Ensures an {@link EOFException} is thrown if {@link #available()} is called
 * when a channel is closed. This helps an optimisation used in UniTTY to work
 * correctly and is valid according to Java API documentation (available can
 * either return 0 or throw exception when at end of stream, we need exception).
 */
public class SshChannelInputStream extends FilterInputStream {

	private SshStreamChannel<?, ?> channel;

	/**
	 * Constructor.
	 * 
	 * @param in original stream
	 * @param channel channel to derive open status from
	 */
	public SshChannelInputStream(InputStream in, SshStreamChannel<?, ?> channel) {
		super(in);
		this.channel = channel;
	}

	public int available() throws IOException {
		if (!channel.isOpen())
			throw new EOFException();

		return super.available();
	}
}
