package net.sf.sshapi;

public abstract class AbstractForwardingChannel<C extends SshClient>
		extends AbstractSshStreamChannel<SshChannelListener<SshChannel>, SshChannel> implements SshChannel {
	protected String hostname;
	protected int port;
	protected C client;

	protected AbstractForwardingChannel(C client, SshProvider provider, SshConfiguration configuration, String hostname,
			int port) {
		super(provider, configuration);
		this.client = client;
		this.hostname = hostname;
		this.port = port;
	}

	@Override
	public String getName() {
		return "direct-tcpip";
	}

	@Override
	public ChannelData getChannelData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean sendRequest(String requesttype, boolean wantreply, byte[] requestdata) throws SshException {
		throw new UnsupportedOperationException();
	}
}