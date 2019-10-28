package net.sf.sshapi;

import java.io.IOException;
import java.util.Objects;

public abstract class AbstractSshExtendedChannel<L extends SshChannelListener<C>, C extends SshStreamChannel<L, C>>
		extends AbstractSshStreamChannel<L, C> implements SshExtendedChannel<L, C> {
	private SshInput errInput;
	private Thread errThread;

	protected AbstractSshExtendedChannel(SshProvider provider, SshConfiguration configuration) {
		super(provider, configuration);
	}

	@Override
	public final void setErrInput(SshInput errInput) {
		if (!Objects.equals(errInput, this.errInput)) {
			this.errInput = errInput;
			if (errInput == null) {
				errThread.interrupt();
			} else {
				try {
					errThread = pump(errInput, getExtendedInputStream());
				} catch (IOException e) {
					throw new IllegalStateException("Failed to extended input stream.", e);
				}
			}
		}
	}
}