package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractForwardingChannel;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

public class LibsshForwardingChannel extends AbstractForwardingChannel<LibsshClient> {

	private SshLibrary library;
	private ssh_channel channel;
	private InputStream in;
	private OutputStream out;

	public LibsshForwardingChannel(SshLibrary library, LibsshClient client, SshProvider provider,
			SshConfiguration configuration, String hostname, int port) {
		super(client, provider, configuration, hostname, port);
		this.library = library;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return in;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	protected void onOpen() throws SshException {
		channel = library.ssh_channel_new(client.libSshSession);
		if (channel == null) {
			throw new SshException(SshException.GENERAL, "Failed to create channel for local port forward.");
		}

		try {
			int ret = library.channel_open_forward(channel, hostname, port, "localhost", 0);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL,
						"Failed to open channel for local port forward to " + hostname + ":" + port);
			}
			in = new EventFiringInputStream(new LibsshInputStream(library, channel, false), SshDataListener.RECEIVED);
			out = new EventFiringOutputStream(new LibsshOutputStream(library, channel));
		} catch (SshException sshe) {
			library.ssh_channel_free(channel);
			throw sshe;
		}
	}

	protected void onCloseStream() throws SshException {
		library.ssh_channel_send_eof(channel);
		try {
			in.close();
		} catch (IOException e) {
		}
		try {
			out.close();
		} catch (IOException e) {
		}
		if (channel != null) {
			library.ssh_channel_close(channel);
			library.ssh_channel_free(channel);
		}
	}

}
