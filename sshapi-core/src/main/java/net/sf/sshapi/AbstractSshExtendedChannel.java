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
import java.util.Objects;

/**
 * Abstract {@link SshExtendedChannel} implementation.
 *
 * @param <L> listener type
 * @param <C> component type
 */
public abstract class AbstractSshExtendedChannel<L extends SshStreamChannelListener<C>, C extends SshStreamChannel<L, C>>
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

	@Override
	public void sendSignal(Signal signal) throws SshException {
		throw new UnsupportedOperationException();
	}
}