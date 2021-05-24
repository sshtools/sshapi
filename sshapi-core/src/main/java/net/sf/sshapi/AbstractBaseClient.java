package net.sf.sshapi;

import java.io.IOException;
import java.util.concurrent.Future;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Provide basic methods that will not change between client implementations. This
 * is mostly overloaded methods.
 */
public abstract class AbstractBaseClient implements SshClient {
	private SshConfiguration configuration;
	private SshProvider provider;

	/**
	 * Constructor.
	 * 
	 * @param configuration configuration
	 */
	public AbstractBaseClient(SshConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public final SshSCPClient scp() throws SshException {
		SshSCPClient scp = createSCP();
		scp.open();
		return scp;
	}

	@Override
	public final SshCommand command(String command) throws SshException {
		return command(command, null, 0, 0, 0, 0, null);
	}

	@Override
	public final Future<Void> connectLater(String spec, SshAuthenticator... authenticators) {
		return connectLater(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec),
				authenticators);
	}

	@Override
	public final void connect(String spec, SshAuthenticator... authenticators) throws SshException {
		connect(Util.extractUsername(spec), Util.extractHostname(spec), Util.extractPort(spec), authenticators);
	}

	@Override
	public final SshCommand createCommand(String command) throws SshException {
		return createCommand(command, null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshShell createShell() throws SshException {
		return createShell(null, 0, 0, 0, 0, null);
	}

	@Override
	public final SshShell shell() throws SshException {
		return shell(null, 0, 0, 0, 0, null);
	}

	@Override
	public Future<SshShell> shellLater() {
		return shellLater(null, 0, 0, 0, 0, null);
	}

	@Override
	public void closeQuietly() {
		try {
			close();
		} catch (Exception e) {
		}
	}

	@Override
	public final SshConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public final SshProvider getProvider() {
		return provider;
	}

	@Override
	public final void init(SshProvider provider) {
		this.provider = provider;
		if(provider.getCapabilities().contains(Capability.IO_TIMEOUTS)) {
			try {
				setTimeout(getConfiguration().getIoTimeout());
			} catch (IOException e) {
				SshConfiguration.getLogger().debug("Failed to set initial timeout.", e);
			}
		}
		onInit();
	}

	@Override
	public Future<Void> closeLater() {
		return new AbstractFuture<Void>() {
			{
				getProvider().getExecutor().submit(createRunnable());	
			}
			
			@Override
			Void doFuture() throws Exception {
				close();
				return null;
			}
		};
	}
	
	protected void onInit() {
	}
}
