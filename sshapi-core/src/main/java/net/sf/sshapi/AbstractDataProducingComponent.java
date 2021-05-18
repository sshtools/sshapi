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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of {@link SshDataProducingComponent} that provides
 * some default common methods.
 * @param <L> the type of listener
 * @param <C> the type of component
 */
public abstract class AbstractDataProducingComponent<L extends SshLifecycleListener<C>, C extends SshDataProducingComponent<L, C>>
		extends AbstractLifecycleComponentWithEvents<L, C> implements SshDataProducingComponent<L, C> {
	

	protected class EventFiringOutputStream extends FilterOutputStream {

		public EventFiringOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void write(int b) throws IOException {
			super.write(b);
			fireData(SshDataListener.SENT, new byte[] { (byte)b }, 0, 1);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			fireData(SshDataListener.SENT, b, off, len);
		}
		
	}

	protected class EventFiringInputStream extends FilterInputStream {

		private int direction;

		public EventFiringInputStream(InputStream in, int direction) {
			super(in);
			this.direction = direction;
		}
		
		@Override
		public int read() throws IOException {
			int r = super.read();
			if(r != -1) {
				fireData(direction, new byte[] { (byte)r }, 0, 1);
			}
			else
				fireEof();
			return r;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int r = super.read(b, off, len);
			if(r != -1) {
				fireData(direction, b, off, r);
			}
			else
				fireEof();
			return r;
		}
		
		protected void fireEof() {
		}
	}

	private List<SshDataListener<C>> dataListeners = new ArrayList<>();
	
	protected AbstractDataProducingComponent(SshProvider provider) {
		super(provider);
	}

	public final synchronized void addDataListener(SshDataListener<C> listener) {
		dataListeners.add(listener);
	}

	public final synchronized void removeDataListener(SshDataListener<C> listener) {
		dataListeners.remove(listener);
	}

	@SuppressWarnings("unchecked")
	protected void fireData(int direction, byte[] buf, int off, int len) {
		for (int i = dataListeners.size() - 1; i >= 0; i--)
			dataListeners.get(i).data((C) this, direction, buf, off, len);
	}
}
