package net.sf.sshapi.maven.wagon;

import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshProxyServerDetails;
import net.sf.sshapi.SshProxyServerDetails.Type;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.sftp.SftpClient;
import net.sf.sshapi.sftp.SftpException;
import net.sf.sshapi.sftp.SftpFile;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.resource.Resource;

public class SftpWagon extends StreamWagon {

	private SshClient client;
	private SftpClient sftp;

	private void setFromProxyInfo(ProxyInfo proxyInfo, SshConfiguration config) {
		String type = proxyInfo.getType();
		Type sshapiType = type.equals(ProxyInfo.PROXY_HTTP) ? Type.HTTP : (type.equals(ProxyInfo.PROXY_SOCKS4) ? Type.SOCKS4
			: Type.SOCKS5);
		config.setProxyServer(new SshProxyServerDetails(sshapiType, proxyInfo.getHost(), proxyInfo.getPort(), proxyInfo
			.getUserName(), proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
	}

	public void closeConnection() throws ConnectionException {
		if (client == null || !client.isConnected()) {
			throw new ConnectionException("Not connected.");
		}
		try {
			try {
				if (sftp != null) {
					sftp.close();
				}
			} finally {
				fireSessionLoggedOff();
				client.disconnect();
			}
		} catch (SshException e) {
			throw new ConnectionException("Failed to disconnect.", e);
		}
	}

	protected void openConnectionInternal() throws ConnectionException, AuthenticationException {

		SshConfiguration config = new SshConfiguration();

		// Configure proxies
		ProxyInfo pi = getProxyInfo();
		if (pi != null) {
			setFromProxyInfo(pi, config);
		}

		// Create the provider

		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);

		// Set up host key validator
		if (provider.getCapabilities().contains(Capability.HOST_KEY_MANAGEMENT)) {
			try {
				config.setHostKeyValidator(new ConsoleHostKeyValidator(provider.createHostKeyManager(config)));
			} catch (SshException e) {
				throw new ConnectionException("Failed to configure host key validator.");
			}
		} else {
			config.setHostKeyValidator(new ConsoleHostKeyValidator());
		}

		// Create client
		client = provider.createClient(config);
		try {
			client.connect(authenticationInfo == null ? System.getProperty("user.name") : authenticationInfo.getUserName(),
				repository.getHost(), repository.getPort());
		} catch (SshException e) {
			throw new ConnectionException("Failed to connect to SSH server.", e);
		}

		// Authenticate

		List authenticators = new ArrayList();
		if (authenticationInfo != null) {
			if (authenticationInfo.getPrivateKey() != null) {

			}
			if (authenticationInfo.getPassword() != null && !authenticationInfo.getPassword().equals("")) {
				authenticators.add(new SimplePasswordAuthenticator(authenticationInfo.getPassword().toCharArray()));
			}
		}
		try {
			client.authenticate((SshAuthenticator[]) authenticators.toArray(new SshAuthenticator[0]));
			fireSessionLoggedIn();
		} catch (SshException e) {
			try {
				if (e.getCode() == SshException.AUTHENTICATION_FAILED) {
					throw new AuthenticationException("Authentication failed.", e);
				} else {
					throw new ConnectionException("Authentication did not complete.", e);
				}
			} finally {
				disconnect();
			}
		}

	}

	public void fillInputData(InputData inputData) throws TransferFailedException, ResourceDoesNotExistException,
			AuthorizationException {
		Resource resource = inputData.getResource();
		try {
			SftpFile file = sftp.stat(resource.getName());
			resource.setContentLength(file.getSize());
			resource.setLastModified(file.getLastModified());
			inputData.setInputStream(sftp.get(resource.getName()));
		} catch (SshException sftpe) {
			if (sftpe.getCode() == SftpException.SSH_FX_NO_SUCH_FILE) {
				throw new ResourceDoesNotExistException(resource.getName() + " does not exist.");
			} else {
				throw new TransferFailedException("Failed to get resource " + resource.getName() + ".", sftpe);
			}
		}

	}

	public void fillOutputData(OutputData outputData) throws TransferFailedException {
		Resource resource = outputData.getResource();
		try {
			outputData.setOutputStream(sftp.put(resource.getName(), 0644));
		} catch (SshException sftpe) {

			throw new TransferFailedException("Failed to put resource " + resource.getName() + ".", sftpe);
		}

	}

}
