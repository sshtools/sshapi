/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of {@link SshDataProducingComponent} that provides
 * some default common methods.
 */
public abstract class AbstractDataProducingComponent extends AbstractLifecycleComponentWithEvents implements
		SshDataProducingComponent {

	private List dataListeners = new ArrayList();

	public final synchronized void addDataListener(SshDataListener listener) {
		dataListeners.add(listener);
	}

	public final synchronized void removeDataListener(SshDataListener listener) {
		dataListeners.remove(listener);
	}

	protected void fireData(int direction, byte[] buf, int off, int len) {
		for (Iterator i = new ArrayList(dataListeners).iterator(); i.hasNext();) {
			((SshDataListener) i.next()).data(this, direction, buf, off, len);
		}
	}
}
