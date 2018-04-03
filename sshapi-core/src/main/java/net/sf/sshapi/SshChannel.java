package net.sf.sshapi;

/**
 * Interface for custom channels created by {@link SshChannelHandler}
 * implementations.
 */
public interface SshChannel extends SshStreamChannel {

	/**
	 * Encapsulate details needed for channel creation
	 */
	public interface ChannelData {
		/**
		 * Get the request data
		 * 
		 * @return request data
		 */
		byte[] getRequestData();

		/**
		 * Get the window size.
		 * 
		 * @return window size
		 */
		int getWindowSize();

		/**
		 * Get the packet size.
		 * 
		 * @return package size
		 */
		int getPacketSize();

		/**
		 * Get the timeout to use for opening the channel.
		 * 
		 * @return timeout
		 */
		long getTimeout();

		/**
		 * Called after the channel has been created by the factory. There is no
		 * need to call this method directly, but it can be overridden to return
		 * data that should be returned in the SSH_MSG_CHANNEL_OPEN_CONFIRMATION
		 * message.
		 * 
		 * @return data that should be returned in the
		 *         SSH_MSG_CHANNEL_OPEN_CONFIRMATION message
		 * @throws IOException
		 */
		byte[] create();
	}
	
	/**
	 * Get the channel name
	 * 
	 * @return name
	 */
	String getName();
	
	/**
	 * Get the {@link ChannelData} that was used to create this channel
	 * 
	 * @return channel data
	 */
	ChannelData getChannelData();

	/**
	 * Sends a channel request. Many channels have extensions that are specific
	 * to that particular channel type, an example of which is requesting a
	 * pseudo terminal from an interactive session.
	 * 
	 * @param requesttype the name of the request, for example "pty-req"
	 * @param wantreply specifies whether the remote side should send a
	 *            success/failure message
	 * @param requestdata the request data
	 * @return <code>true</code> if the request succeeded and wantreply=true,
	 *         otherwise <code>false</code>
	 * @throws SshException
	 */
	boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException;
}
