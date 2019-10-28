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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.util.Util;

/**
 * Abstract implementation of an {@link SshProvider}, providing some common
 * methods. All providers will probably want to extend this.
 */
public abstract class AbstractProvider implements SshProvider {
	static boolean check(String name, List<String> list) {
		boolean ok = name == null || list.contains(name) || name.equals("none");
		if (!ok) {
			throw new UnsupportedOperationException("Capability " + name + " is not one of " + list);
		}
		return ok;
	}
	private final String name;
	// TODO configurable
	private ExecutorService scheduler = Executors.newCachedThreadPool();

	private boolean supportsDefaultConfiguration = true;

	private final String vendor;

	protected AbstractProvider(String name) {
		this(name, "Unknown");
	}

	protected AbstractProvider(String name, String vendor) {
		this.name = name;
		this.vendor = vendor;
	}

	@Override
	public SshAgent connectToLocalAgent(String application) throws SshException {
		return connectToLocalAgent(application, SshAgent.AUTO_PROTOCOL);
	}

	@Override
	public SshAgent connectToLocalAgent(String application, int protocol) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SshAgent connectToLocalAgent(String application, String location, int socketType, int protocol) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public final SshClient createClient(SshConfiguration configuration) {
		doSupportsConfiguration(configuration);
		checkDefaultConfiguration(configuration);
		SshClient client = doCreateClient(configuration);
		client.init(this);
		return client;
	}

	@Override
	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SshIdentityManager createIdentityManager(SshConfiguration configuration) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getVendor() {
		return vendor;
	}

	@Override
	public String getVersion() {
		return "Unknown";
	}

	@Override
	public SshClient open(SshConfiguration configuration, String spec, SshAuthenticator... authenticators) throws SshException {
		return open(configuration, Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public SshClient open(SshConfiguration configuration, String username, String hostname, int port,
			SshAuthenticator... authenticators) throws SshException {
		SshClient client = createClient(configuration);
		client.connect(username, hostname, port, authenticators);
		return client;
	}

	@Override
	public Future<SshClient> openLater(SshConfiguration configuration, String spec, SshAuthenticator... authenticators) {
		return openLater(configuration, Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public Future<SshClient> openLater(SshConfiguration configuration, String username, String hostname, int port,
			SshAuthenticator... authenticators) {
		return new AbstractFuture<SshClient>() {
			{
				scheduler.execute(createRunnable());
			}

			@Override
			SshClient doFuture() throws Exception {
				return open(configuration, username, hostname, port, authenticators);
			}
		};
	}

	@Override
	public final boolean supportsConfiguration(SshConfiguration configuration) {
		try {
			doSupportsConfiguration(configuration);
			checkDefaultConfiguration(configuration);
			return true;
		} catch (UnsupportedOperationException ueo) {
			return false;
		}
	}

	protected abstract SshClient doCreateClient(SshConfiguration configuration);
	
	protected abstract void doSupportsConfiguration(SshConfiguration configuration);

	protected void setSupportsDefaultConfiguration(boolean supportsDefaultConfiguration) {
		this.supportsDefaultConfiguration = supportsDefaultConfiguration;
	}

	private void checkDefaultConfiguration(SshConfiguration configuration) {
		if (configuration == null) {
			if (supportsDefaultConfiguration) {
				return;
			} else {
				throw new UnsupportedOperationException("Default configuration is not supported. You must supply a configuration.");
			}
		}
		configuration.providerHasCapabilities(this);
		check(configuration.getPreferredClientToServerCipher(), getSupportedCiphers(configuration.getProtocolVersion()));
		check(configuration.getPreferredServerToClientCipher(), getSupportedCiphers(configuration.getProtocolVersion()));
		check(configuration.getPreferredClientToServerMAC(), getSupportedMAC());
		check(configuration.getPreferredServerToClientMAC(), getSupportedMAC());
		check(configuration.getPreferredClientToServerCompression(), getSupportedCompression());
		check(configuration.getPreferredServerToClientCompression(), getSupportedCompression());
		if (configuration.getProxyServer() != null) {
			SshProxyServerDetails proxy = configuration.getProxyServer();
			if (proxy.getType().equals(SshProxyServerDetails.Type.HTTP) && !getCapabilities().contains(Capability.HTTP_PROXY)) {
				throw new UnsupportedOperationException("HTTP proxy is not supported.");
			}
			if (proxy.getType().equals(SshProxyServerDetails.Type.SOCKS4) && !getCapabilities().contains(Capability.SOCKS4_PROXY)) {
				throw new UnsupportedOperationException("SOCKS4 proxy is not supported.");
			}
			if (proxy.getType().equals(SshProxyServerDetails.Type.SOCKS5) && !getCapabilities().contains(Capability.SOCKS5_PROXY)) {
				throw new UnsupportedOperationException("SOCKS5 proxy is not supported.");
			}
		}
	}

	@Override
	public ExecutorService getExecutor() {
		return scheduler;
	}
	
	
}
