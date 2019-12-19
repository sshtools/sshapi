package net.sf.sshapi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extension of a {@link SshStreamChannel} that adds the Extended Input Stream,
 * used for STDERR.
 * 
 */

public interface SshExtendedChannel<L extends SshLifecycleListener<C>, C extends SshDataProducingComponent<L, C>>
		extends SshStreamChannel<L, C> {
	

	public enum Signal {
		ABRT, ALRM, FPE, HUP, ILL, INT, KILL, PIPE, QUIT, SEGV, TERM, USR1, USR2
	}
	
	/**
	 * Set the callback that will be invoked when bytes are available on the error stream of this
	 * channel. This is the non-blocking variant of using {@link #getErrorStream()}.
	 * 
	 *  @param input handler
	 */
	void setErrInput(SshInput input);
	
	/**
	 * Get the extended input stream.
	 * 
	 * @return extended input stream
	 * @throws IOException
	 */
	InputStream getExtendedInputStream() throws IOException;

	/**
	 * Get the exit code of this command or shell (when known).
	 * 
	 * @return exist code
	 * @throws IOException
	 */
	int exitCode() throws IOException;
	
	/**
	 * Send a signal, if supported ({@link Capability#SIGNALS} must be present).
	 * 
	 * @param signal signal
	 */
	void sendSignal(Signal signal) throws SshException;
}