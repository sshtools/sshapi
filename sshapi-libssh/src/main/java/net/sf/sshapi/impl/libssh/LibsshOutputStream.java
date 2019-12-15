package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.OutputStream;

import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

import com.sun.jna.Memory;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.Logger;
import net.sf.sshapi.Logger.Level;

public class LibsshOutputStream extends OutputStream {

	private static final Logger LOG = SshConfiguration.getLogger();
	private ssh_channel channel;
	private SshLibrary library;
	private boolean closed;

	public LibsshOutputStream(SshLibrary library, ssh_channel channel) {
		this.channel = channel;
		this.library = library;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Stream closed.");
		}
		Memory m = new Memory(len);
		m.write(0, b, off, len);
		if (LOG.isDebug())
			LOG.debug("Writing {0} bytes", len);
		int written = library.ssh_channel_write(channel, m, len);
		if (written < 1) {
			throw new IOException("Failed to write.");
		}
		if (LOG.isDebug())
			LOG.debug("Written {0} bytes", len);
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Stream closed.");
		}
		Memory m = new Memory(1);
		m.setByte(0, (byte) b);
		if (LOG.isDebug())
			LOG.debug("Writing 1 bytes");
		int written = library.ssh_channel_write(channel, m, 1);
		if (written != 1) {
			throw new IOException("Failed to write.");
		}
		if (LOG.isDebug())
			LOG.debug("Written 1 bytes");
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (!closed) {
			closed = true;
			if (LOG.isDebug())
				LOG.debug("Sending EOF");
			library.ssh_channel_send_eof(channel);
			if (LOG.isDebug())
				LOG.debug("Sent EOF, closed output stream");
		}
		else
			if (LOG.isDebug())
				LOG.debug("Request to close stream that is already closed.");
	}

}