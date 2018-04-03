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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.identity.SshPublicKeySubsystem;

/**
 * Abstract implementation of a {@link SshClient}. All provider client
 * implementations will probably want to extend this as it provides some basic
 * common services.
 */
public abstract class AbstractClient implements SshClient {

	private SshProvider provider;
	private SshConfiguration configuration;
	private List portForwardlisteners = new ArrayList();

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 */
	public AbstractClient(SshConfiguration configuration) {
		this.configuration = configuration;
	}

	public synchronized void addPortForwardListener(SshPortForwardListener listener) {
		portForwardlisteners.add(listener);
	}

	public synchronized void removePortForwardListener(SshPortForwardListener listener) {
		portForwardlisteners.remove(listener);
	}

	public SshConfiguration getConfiguration() {
		return configuration;
	}

	public final void init(SshProvider provider) {
		this.provider = provider;
	}

	public final SshProvider getProvider() {
		return provider;
	}

	public final boolean authenticate(SshAuthenticator authenticator) throws SshException {
		return authenticate(new SshAuthenticator[] { authenticator });
	}

	public void addChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	public void removeChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	protected Map createAuthenticatorMap(SshAuthenticator[] authenticators) {
		Map authenticatorMap = new HashMap();
		for (int i = 0; i < authenticators.length; i++) {
			if (authenticatorMap.containsKey(authenticators[i].getTypeName())) {
				throw new IllegalArgumentException("Two authenticators using the name '" + authenticators[i].getTypeName()
					+ "' have been provided.");
			}
			authenticatorMap.put(authenticators[i].getTypeName(), authenticators[i]);
		}
		return authenticatorMap;
	}

	protected void firePortForwardChannelOpened(int type, SshPortForwardTunnel channel) {
		for (Iterator i = new ArrayList(portForwardlisteners).iterator(); i.hasNext();) {
			((SshPortForwardListener) i.next()).channelOpened(type, channel);
		}
	}

	protected void firePortForwardChannelClosed(int type, SshPortForwardTunnel channel) {
		for (Iterator i = new ArrayList(portForwardlisteners).iterator(); i.hasNext();) {
			((SshPortForwardListener) i.next()).channelClosed(type, channel);
		}
	}

	public SshPublicKeySubsystem createPublicKeySubsystem() throws SshException {
		throw new UnsupportedOperationException();
	}

	public void setTimeout(int timeout) throws IOException {
		throw new UnsupportedOperationException();
	}

	public int getTimeout() throws IOException {
		throw new UnsupportedOperationException();
	}
}
