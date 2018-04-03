package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.OutputStream;

import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

import com.sun.jna.Memory;

public class LibsshOutputStream extends OutputStream {

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
		int written = library.ssh_channel_write(channel, m, len);
		if (written < 1) {
			throw new IOException("Failed to write.");
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (closed) {
			throw new IOException("Stream closed.");
		}
		Memory m = new Memory(1);
		m.setByte(0, (byte) b);
		int written = library.ssh_channel_write(channel, m, 1);
		if (written != 1) {
			throw new IOException("Failed to write.");
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		closed = true;
	}

}