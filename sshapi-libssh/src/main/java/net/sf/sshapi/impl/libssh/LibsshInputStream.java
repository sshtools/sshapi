package net.sf.sshapi.impl.libssh;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.sun.jna.Memory;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshConfiguration;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

public class LibsshInputStream extends InputStream {

	private static final Logger LOG = SshConfiguration.getLogger();
	private ssh_channel channel;
	private SshLibrary library;
	private boolean stderr;
	private boolean closed;

	public LibsshInputStream(SshLibrary library, ssh_channel channel, boolean stderr) {
		this.channel = channel;
		this.library = library;
		this.stderr = stderr;
	}

	@Override
	public int available() throws IOException {
		int i = checkChannel();
		if (i == -1) {
			throw new EOFException();
		}
		if (LOG.isDebug())
			LOG.debug("Polling channel");
		int av = library.ssh_channel_poll(channel, stderr ? 1 : 0);
		if (LOG.isDebug())
			LOG.debug("Got {0} bytes available", av);
		return av;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int i = checkChannel();
		if (i == -1) {
			return -1;
		}
		Memory m = new Memory(len);
		if (LOG.isDebug())
			LOG.debug("Reading {0} bytes", len);
		int read = library.ssh_channel_read(channel, m, len, stderr ? 1 : 0);
		if (read < 0) {
			throw new IOException("I/O Error");
		}
		if (read == 0) {
			return -1;
		}
		byte[] buf = m.getByteArray(0, read);
		System.arraycopy(buf, 0, b, off, read);
		if (LOG.isDebug())
			LOG.debug("Read {0} bytes.", read);
		return read;
	}

	@Override
	public int read() throws IOException {
		int i = checkChannel();
		if (i == -1) {
			return -1;
		}
		Memory m = new Memory(1);
		if (LOG.isDebug())
			LOG.debug("Reading 1 bytes");
		int read = library.ssh_channel_read(channel, m, 1, stderr ? 1 : 0);
		if (read < 0) {
			throw new IOException("I/O Error");
		}
		if (LOG.isDebug())
			LOG.debug("Read 1 bytes.");
		return m.getByte(0);
	}

	private int checkChannel() throws IOException {
		if (closed) {
			throw new IOException("Stream is closed.");
		}
		if (library.ssh_channel_is_eof(channel) == 1) {
			return -1;
		}
		if (library.ssh_channel_is_closed(channel) == 1) {
			throw new IOException("Channel is closed.");
		}
		return 0;
	}

	@Override
	public void close() throws IOException {
		super.close();
		closed = true;
		if (LOG.isDebug())
			LOG.debug("Closed input stream");
	}

}