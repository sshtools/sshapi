package net.sf.sshapi.impl.libssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.Logger;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import ssh.SshLibrary;
import ssh.SshLibrary.ssh_channel;
import ssh.SshLibrary.ssh_session;

class LibsshSshCommand extends AbstractSshExtendedChannel<SshChannelListener<SshCommand>, SshCommand> implements SshCommand {
	private static final Logger LOG = SshConfiguration.getLogger();
	private InputStream in;
	private InputStream ext;
	private OutputStream out;
	private ssh_channel channel;
	private SshLibrary library;
	private String command;
	private ssh_session libSshSession;
	private String termType;
	private int cols;
	private int rows;

	public LibsshSshCommand(SshProvider provider, SshConfiguration configuration, ssh_session libSshSession, SshLibrary library,
			String command, String termType, int cols, int rows) {
		super(provider, configuration);
		this.library = library;
		this.command = command;
		this.libSshSession = libSshSession;
		this.termType = termType;
		this.cols = cols;
		this.rows = rows;
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
	public InputStream getExtendedInputStream() throws IOException {
		return ext;
	}

	@Override
	public final void onOpenStream() throws SshException {
		if(LOG.isDebug())
			LOG.debug("Opening channel {0}", hashCode());
		channel = library.ssh_channel_new(libSshSession);
		if (channel == null) {
			throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to open channel for command.");
		}
		try {
			if(LOG.isDebug())
				LOG.debug("Opening channel {0}, opening session", hashCode());
			int ret = library.ssh_channel_open_session(channel);
			if (ret != SshLibrary.SSH_OK) {
				throw new SshException(SshException.GENERAL, "Failed to open session for command channel.");
			}
			if (termType != null) {
				if(LOG.isDebug())
					LOG.debug("Requesting pty for {0}. Terminal {1} ({2} x {3})", hashCode(), termType, cols, rows);
				ret = library.ssh_channel_request_pty_size(channel, termType, cols, rows);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.FAILED_TO_OPEN_SHELL, "Failed to set PTY size");
				}
			}
			try {
				if(LOG.isDebug())
					LOG.debug("Executing {0} for channel {1}", command, hashCode());
				ret = library.ssh_channel_request_exec(channel, command);
				if (ret != SshLibrary.SSH_OK) {
					throw new SshException(SshException.GENERAL, "Failed to execute command.");
				}
				if(LOG.isDebug())
					LOG.debug("Getting streams for channel {0}", hashCode());
				in = new EventFiringInputStream(new LibsshInputStream(library, channel, false), SshDataListener.RECEIVED);
				out = new EventFiringOutputStream(new LibsshOutputStream(library, channel));
				ext = new EventFiringInputStream(new LibsshInputStream(library, channel, true), SshDataListener.EXTENDED);
			} catch (SshException sshe) {
				library.ssh_channel_close(channel);
				throw sshe;
			}
		} catch (SshException sshe) {
			library.ssh_channel_free(channel);
			throw sshe;
		}
		if(LOG.isDebug())
			LOG.debug("Opened channel {0}", hashCode());
	}

	@Override
	public void onCloseStream() throws SshException {
		try {
			out.close();
		} catch (IOException e) {
		}
		try {
			in.close();
		} catch (IOException e) {
		}
		if (channel != null) {
			if(LOG.isDebug())
				LOG.debug("Closing {0} channel.", hashCode());
			library.ssh_channel_close(channel);
			if(LOG.isDebug())
				LOG.debug("Freeing {0} channel.", hashCode());
			library.ssh_channel_free(channel);
		}
	}

	@Override
	public int exitCode() throws IOException {
		return library.ssh_channel_get_exit_status(channel);
	}
}