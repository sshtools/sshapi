/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.sf.sshapi;

/**
 * The Class AbstractForwardingChannel.
 *
 * @param <C> the client type
 */
public abstract class AbstractForwardingChannel<C extends SshClient>
		extends AbstractSshStreamChannel<SshCustomChannelListener, SshCustomChannel> implements SshCustomChannel {
	
	/** The hostname. */
	protected String hostname;
	
	/** The port. */
	protected int port;
	
	/** The client. */
	protected C client;

	/**
	 * Instantiates a new abstract forwarding channel.
	 *
	 * @param client the client
	 * @param provider the provider
	 * @param configuration the configuration
	 * @param hostname the hostname
	 * @param port the port
	 */
	protected AbstractForwardingChannel(C client, SshProvider provider, SshConfiguration configuration, String hostname,
			int port) {
		super(provider, configuration);
		this.client = client;
		this.hostname = hostname;
		this.port = port;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return "direct-tcpip";
	}

	/**
	 * Gets the channel data.
	 *
	 * @return the channel data
	 */
	@Override
	public ChannelData getChannelData() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Send request.
	 *
	 * @param requesttype the requesttype
	 * @param wantreply the wantreply
	 * @param requestdata the requestdata
	 * @return true, if successful
	 * @throws SshException the ssh exception
	 */
	@Override
	public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
		throw new UnsupportedOperationException();
	}
}