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
package net.sf.sshapi.impl.ganymed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.sshapi.AbstractDataProducingComponent;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshExtendedStreamChannel;
import net.sf.sshapi.util.Util;
import ch.ethz.ssh2.Session;

abstract class GanymedStreamChannel extends AbstractDataProducingComponent implements SshExtendedStreamChannel {

	private final Session session;
	private final SshConfiguration configuration;

	public GanymedStreamChannel(SshConfiguration configuration, Session channel) throws SshException {
		this.session = channel;
		this.configuration = configuration;
	}

	public int exitCode() throws IOException {
		Integer t = session.getExitStatus();
		return t == null ? -1 : t.intValue();
	}

	public InputStream getInputStream() throws IOException {
		return session.getStdout();
	}
	public OutputStream getOutputStream() throws IOException {
		return session.getStdin();
	}

	public InputStream getExtendedInputStream() throws IOException {
		return session.getStderr();
	}

	protected Session getSession() {
		return session;
	}

	public void onClose() throws SshException {
		session.close();
	}

	public final void onOpen() throws SshException {
		if (!Util.nullOrTrimmedBlank(configuration.getX11Host())) {
			boolean singleConnection = Boolean.parseBoolean(configuration.getProperties().getProperty(
				GanymedSshProvider.CFG_SINGLE_X11_CONNECTION, "false"));
			try {
				session.requestX11Forwarding(configuration.getX11Host(), configuration.getX11Port(), configuration.getX11Cookie(),
					singleConnection);
			} catch (IOException e) {
				throw new SshException(SshException.IO_ERROR, e);
			}
		}
		onChannelOpen();
	}

	public abstract void onChannelOpen() throws SshException;

}
