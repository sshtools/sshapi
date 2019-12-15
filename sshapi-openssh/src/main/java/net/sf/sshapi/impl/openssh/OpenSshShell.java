package net.sf.sshapi.impl.openssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.AbstractSshExtendedChannel;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

class OpenSshShell extends AbstractSshExtendedChannel<SshChannelListener<SshShell>, SshShell> implements SshShell {
	private ProcessBuilder pb;
	private Process process;
	private final String termType;
	private OpenSshClient client;

	OpenSshShell(OpenSshClient client, ProcessBuilder pb, String termType) {
		super(client.getProvider(), client.getConfiguration());
		this.pb = pb;
		this.termType =termType;
		this.client = client;
	}

	@Override
	public InputStream getExtendedInputStream() throws IOException {
		return process.getErrorStream();
	}

	@Override
	public int exitCode() throws IOException {
		return process.exitValue();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return process.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return process.getOutputStream();
	}

	@Override
	public void requestPseudoTerminalChange(int width, int height, int pixw, int pixh) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void onCloseStream() throws SshException {
		try {
			int result = process.waitFor();
			if (result != 0)
				SshConfiguration.getLogger().warn("Ssh client exited with non-zero code {0}", result);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			process = null;
		}
	}

	@Override
	protected void onOpen() throws SshException {
		try {
			if(termType != null && termType.length() > 0) {
				pb.environment().put("TERM", termType);
				pb.command().add(3, "-t");
			}
			else
				pb.command().add(3, "-T");
			process = client.setupAuthentication(pb).start();
			try {
				if (process.waitFor(3, TimeUnit.SECONDS))
					throw new SshException(SshException.AUTHENTICATION_FAILED);
			} catch (IllegalThreadStateException | InterruptedException e) {
			}
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, "Failed to connect.", ioe);
		}
	}
}