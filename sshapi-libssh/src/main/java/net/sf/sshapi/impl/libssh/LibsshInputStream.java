package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;

import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;

import com.sun.jna.Memory;

public class LibsshInputStream extends InputStream {

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
	public int read(byte[] b, int off, int len) throws IOException {
		int i = checkChannel();
		if (i == -1) {
			return -1;
		}
		Memory m = new Memory(len);
		int read = library.ssh_channel_read(channel, m, len, stderr ? 1 : 0);
		if (read < 0) {
			throw new IOException("I/O Error");
		}
		if(read == 0) {
			return -1;
		}
		byte[] buf = m.getByteArray(0, read);
		System.arraycopy(buf, 0, b, off, read);
		System.out.println("X: " + new String(buf, 0, read));
		return read;
	}

	@Override
	public int read() throws IOException {
		int i = checkChannel();
		if (i == -1) {
			return -1;
		}
		Memory m = new Memory(1);
		int read = library.ssh_channel_read(channel, m, 1, stderr ? 1 : 0);
		if (read < 0) {
			throw new IOException("I/O Error");
		}
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
	}

}