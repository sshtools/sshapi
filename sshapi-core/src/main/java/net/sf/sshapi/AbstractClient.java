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
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.forwarding.SshPortForwardTunnel;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.util.Util;

/**
 * Abstract implementation of a {@link SshClient}. All provider client
 * implementations will probably want to extend this as it provides some basic
 * common services.
 */
public abstract class AbstractClient implements SshClient {

	private SshProvider provider;
	private SshConfiguration configuration;
	private List<SshPortForwardListener> portForwardlisteners = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param configuration
	 *            configuration
	 */
	public AbstractClient(SshConfiguration configuration) {
		this.configuration = configuration;
	}

	public final void connect(String spec, SshAuthenticator... authenticators)
			throws SshException {
		connect(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	public final void connect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		if (isConnected()) {
			throw new net.sf.sshapi.SshException(net.sf.sshapi.SshException.ALREADY_OPEN, "Already connected.");
		}
		doConnect(username, hostname, port, authenticators);
		if (authenticators.length > 0) {
			try {
				for (int i = 0; i < getConfiguration().getMaxAuthAttempts() && !authenticate(authenticators); i++)
					;
				if (!isAuthenticated())
					throw new SshException(SshException.AUTHENTICATION_FAILED);
			} catch (SshException sshe) {
				try {
					close();
				} catch (IOException e) {
				}
				throw sshe;
			} catch (RuntimeException re) {
				try {
					close();
				} catch (IOException ioe) {
				}
				throw re;
			}
		}
	}

	protected abstract void doConnect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException;

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

	@SuppressWarnings("deprecation")
	public void close() throws IOException {
		try {
			disconnect();
		} catch (SshException e) {
			throw new IOException("Failed to close.", e);
		}
	}

	@SuppressWarnings("deprecation")
	public void closeQuietly() {
		try {
			disconnect();
		} catch (Exception e) {
		}
	}

	public SshShell shell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		SshShell shell = createShell(termType, cols, rows, pixWidth, pixHeight, terminalModes);
		shell.open();
		return shell;
	}

	public SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		throw new UnsupportedOperationException("Shell is is not supported in this implementation.");
	}

	public void addChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	public void removeChannelHandler(SshChannelHandler channelFactory) throws SshException {
		throw new UnsupportedOperationException("Channel factories are not supported in this implementation.");
	}

	public SshPortForward createLocalForward(String localBindAddress, int localBindPort, String targetAddress,
			int targetPort) throws SshException {
		throw new UnsupportedOperationException("Local forwarding is not supported in this implementation.");
	}

	public SshPortForward localForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		SshPortForward fwd = createLocalForward(localBindAddress, localBindPort, targetAddress, targetPort);
		fwd.open();
		return fwd;
	}

	public SshPortForward createRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		throw new UnsupportedOperationException("Remote forwarding is not supported in this implementation.");
	}

	public SshPortForward remoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		SshPortForward fwd = createRemoteForward(remoteBindAddress, remoteBindPort, targetAddress, targetPort);
		fwd.open();
		return fwd;
	}

	public SshCommand command(String command) throws SshException {
		SshCommand sshCommand = createCommand(command);
		sshCommand.open();
		return sshCommand;
	}

	public SshCommand createCommand(String command) throws SshException {
		throw new UnsupportedOperationException("Commands are not supported in this implementation.");
	}

	public SocketFactory createTunneledSocketFactory() throws SshException {
		throw new UnsupportedOperationException("Tunneled socket factory is not supported in this implementation.");
	}

	public final SshSCPClient createSCPClient() throws SshException {
		return createSCP();
	}

	public SshSCPClient createSCP() throws SshException {
		throw new UnsupportedOperationException();
	}

	public SshSCPClient scp() throws SshException {
		SshSCPClient scp = createSCP();
		scp.open();
		return scp;
	}

	public final SftpClient createSftpClient() throws SshException {
		return createSftp();
	}

	public SftpClient createSftp() throws SshException {
		throw new UnsupportedOperationException();
	}

	public SftpClient sftp() throws SshException {
		SftpClient client = createSftp();
		client.open();
		return client;
	}

	protected Map<String, SshAuthenticator> createAuthenticatorMap(SshAuthenticator[] authenticators) {
		Map<String, SshAuthenticator> authenticatorMap = new HashMap<>();
		for (int i = 0; i < authenticators.length; i++) {
			if (authenticatorMap.containsKey(authenticators[i].getTypeName())) {
				throw new IllegalArgumentException("Two authenticators using the name '"
						+ authenticators[i].getTypeName() + "' have been provided.");
			}
			authenticatorMap.put(authenticators[i].getTypeName(), authenticators[i]);
		}
		return authenticatorMap;
	}

	protected void firePortForwardChannelOpened(int type, SshPortForwardTunnel channel) {
		for (int i = portForwardlisteners.size() - 1; i >= 0; i--)
			portForwardlisteners.get(i).channelOpened(type, channel);
	}

	protected void firePortForwardChannelClosed(int type, SshPortForwardTunnel channel) {
		for (int i = portForwardlisteners.size() - 1; i >= 0; i--)
			portForwardlisteners.get(i).channelClosed(type, channel);
	}

	public SshPublicKeySubsystem createPublicKeySubsystem() throws SshException {
		throw new UnsupportedOperationException();
	}

	public SshPublicKeySubsystem publicKeySubsystem() throws SshException {
		SshPublicKeySubsystem ks = createPublicKeySubsystem();
		ks.open();
		return ks;
	}

	public void setTimeout(int timeout) throws IOException {
		throw new UnsupportedOperationException();
	}

	public int getTimeout() throws IOException {
		throw new UnsupportedOperationException();
	}
}
