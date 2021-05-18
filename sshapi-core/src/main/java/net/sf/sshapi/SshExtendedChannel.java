/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.sf.sshapi;

import java.io.IOException;
import java.io.InputStream;

// TODO: Auto-generated Javadoc
/**
 * Extension of a {@link SshStreamChannel} that adds the Extended Input Stream,
 * used for STDERR.
 * 
 * @param <L> listener type
 * @param <C> component type
 * 
 */

public interface SshExtendedChannel<L extends SshLifecycleListener<C>, C extends SshDataProducingComponent<L, C>>
		extends SshStreamChannel<L, C> {

	/**
	 * Signal.
	 */
	public enum Signal {

		/** abrt. */
		ABRT,
		/** alrm. */
		ALRM,
		/** fpe. */
		FPE,
		/** hup. */
		HUP,
		/** ill. */
		ILL,
		/** int. */
		INT,
		/** kill. */
		KILL,
		/** pipe. */
		PIPE,
		/** quit. */
		QUIT,
		/** segv. */
		SEGV,
		/** term. */
		TERM,
		/** usr1. */
		USR1,
		/** usr2. */
		USR2
	}

	/**
	 * Set the callback that will be invoked when bytes are available on the error
	 * stream of this channel. This is the non-blocking variant of using
	 * {@link #getErrorStream()}.
	 * 
	 * @param input handler
	 */
	void setErrInput(SshInput input);

	/**
	 * Get the extended input stream.
	 *
	 * @return extended input stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	InputStream getExtendedInputStream() throws IOException;

	/**
	 * Get the exit code of this command or shell (when known).
	 *
	 * @return exist code
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	int exitCode() throws IOException;

	/**
	 * Send a signal, if supported ({@link Capability#SIGNALS} must be present).
	 * 
	 * @param signal signal
	 * @throws SshException on error
	 */
	void sendSignal(Signal signal) throws SshException;
}