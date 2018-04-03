package net.sf.sshapi;

import java.io.IOException;

import net.sf.sshapi.SshChannel.ChannelData;

/**
 * SSH allows custom channels to be handled. Implement this interface, and add
 * the handler to the {@link SshClient} instance. When the server requests a
 * channel with this name, {@link #channelCreated(SshChannel)} will be called.
 * 
 */
public interface SshChannelHandler {

	/**
	 * Get the names of the channels this factory supports.
	 * 
	 * @return channel names
	 */
	String[] getSupportChannelNames();

	/**
	 * Called when a new channel with a supported name has been created.
	 * 
	 * @param channelName channel name
	 * @param requestData request data
	 * @return data to send or <code>null</code> for none
	 */
	ChannelData createChannel(String channelName, byte[] requestData);

	/**
	 * Called when a new channel with a supported name has been created. The client code
	 * can hook in here to add listeners.
	 * 
	 * @param channel channel
	 */
	void channelCreated(SshChannel channel) throws IOException;
}
