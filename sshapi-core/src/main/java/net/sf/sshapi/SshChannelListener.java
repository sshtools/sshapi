package net.sf.sshapi;


public interface SshChannelListener extends SshLifecycleListener {

	/**
	 * A channel has been reached EOF.
	 * 
	 * @param channel the channel at EOF.
	 */
	void eof(SshChannel channel);


	/**
	 * The remote side sent a channel request.
	 * 
	 * @param channel channel
	 * @param requesttype request type
	 * @param wantreply server requested a reply
	 * @param requestdata request data
	 * @return send a failure message when <code>true</code>
	 */
	boolean request(SshChannel channel, String requesttype, boolean wantreply, byte[] requestdata);
}
