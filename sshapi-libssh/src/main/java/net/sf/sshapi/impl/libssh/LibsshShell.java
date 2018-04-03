package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractLifecycleComponentWithEvents;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;

// Crazy name :)
public class LibsshShell extends AbstractLifecycleComponentWithEvents implements SshShell {

	private LibsshInputStream in;
	private LibsshInputStream ext;
	private LibsshOutputStream out;
	private ssh_channel channel;
	private SshLibrary library;
	private String termType;
	private int cols;
	private int rows;
	private boolean useExtendedStream;
	private ssh_session libSshSession;

	public LibsshShell(ssh_session libSshSession, SshLibrary library, String termType, int cols, int rows,
			boolean useExtendedStream) {
		this.libSshSession = libSshSession;
		this.library = library;
		this.termType = termType;
		this.cols = cols;
		this.rows = rows;
		this.useExtendedStream = useExtendedStream;
	}

	public InputStream getInputStream() throws IOException {
		return in;
	}

	public OutputStream getOutputStream() throws IOException {
		return out;
	}

	public void addDataListener(SshDataListener listener) {
	}

	public void removeDataListener(SshDataListener listener) {
	}

	public void onOpen() throws SshException {

		channel = library.ssh_channel_new(libSshSession);
		if (channel == null) {
			throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to open channel for shell.");
		}

		try {
			int ret = library.ssh_channel_open_session(channel);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to open session for shell channel.");
			}

			if (termType != null) {
				ret = library.ssh_channel_request_pty_size(channel, termType, cols, rows);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to set PTY size");
				}
			}

			try {
				ret = library.ssh_channel_request_shell(channel);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.FAILED_TO_OPEN_SHELL);
				}

				in = new LibsshInputStream(library, channel, false);
				if (useExtendedStream) {
					ext = new LibsshInputStream(library, channel, true);
				}
				out = new LibsshOutputStream(library, channel);

			} catch (SshException sshe) {
				library.ssh_channel_close(channel);
				throw sshe;
			}

		} catch (SshException sshe) {
			library.ssh_channel_free(channel);
			throw sshe;
		}

	}

	public void onClose() throws SshException {
		library.ssh_channel_send_eof(channel);
		try {
			in.close();
		} catch (IOException e) {
		}
		try {
			out.close();
		} catch (IOException e) {
		}
		if (ext != null) {
			try {
				ext.close();
			} catch (IOException e) {
			}
		}
		if (channel != null) {
			library.ssh_channel_close(channel);
			library.ssh_channel_free(channel);
		}
	}

	public InputStream getExtendedInputStream() throws IOException {
		return ext;
	}

	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		int ret = library.ssh_channel_change_pty_size(channel, width, height);
		if (ret != SshLibrary.SSH_OK) {
			throw new SshException("Failed to change terminal size. Err:" + ret);
		}
	}

	public int exitCode() throws IOException {
		return library.ssh_channel_get_exit_status(channel);
	}

}