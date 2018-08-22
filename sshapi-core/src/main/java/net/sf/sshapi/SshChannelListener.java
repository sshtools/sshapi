package net.sf.sshapi;

public interface SshChannelListener<C extends SshDataProducingComponent<?, C>> extends SshLifecycleListener<C> {

	/**
	 * A channel has been reached EOF.
	 * 
	 * @param channel
	 *            the channel at EOF.
	 */
	default void eof(C channel) {
	}

	/**
	 * The remote side sent a channel request.
	 * 
	 * @param channel
	 *            channel
	 * @param requesttype
	 *            request type
	 * @param wantreply
	 *            server requested a reply
	 * @param requestdata
	 *            request data
	 * @return send a failure message when <code>true</code>
	 */
	default boolean request(C channel, String requesttype, boolean wantreply, byte[] requestdata) {
		return false;
	}
}
