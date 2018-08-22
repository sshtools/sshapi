package org.apache.maven.wagon.providers.ssh.sshapi;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshProxyServerDetails;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPasswordAuthenticator;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.DefaultAgentAuthenticator;
import net.sf.sshapi.util.PEMFilePublicKeyAuthenticator;

import org.apache.maven.wagon.CommandExecutionException;
import org.apache.maven.wagon.CommandExecutor;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.Streams;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.WagonConstants;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.providers.ssh.CommandExecutorStreamProcessor;
import org.apache.maven.wagon.providers.ssh.ScpHelper;
import org.apache.maven.wagon.providers.ssh.SshWagon;
import org.apache.maven.wagon.providers.ssh.interactive.InteractiveUserInfo;
import org.apache.maven.wagon.providers.ssh.interactive.NullInteractiveUserInfo;
import org.apache.maven.wagon.providers.ssh.knownhost.KnownHostsProvider;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.util.IOUtil;

/**
 * AbstractJschWagon
 * 
 * @version $Id: AbstractJschWagon.java 1170464 2011-09-14 07:56:38Z olamy $
 */
public abstract class AbstractSSHAPIWagon extends StreamWagon implements SshWagon, CommandExecutor {
	protected ScpHelper sshTool = new ScpHelper(this);

	protected SshClient session;

	/**
	 * @plexus.requirement role-hint="file"
	 */
	private KnownHostsProvider knownHostsProvider;

	/**
	 * @plexus.requirement
	 */
	private InteractiveUserInfo interactiveUserInfo;

	private SshKeyboardInteractiveAuthenticator uIKeyboardInteractive;
	private SshAgent agent;

	private static final int SOCKS5_PROXY_PORT = 1080;

	protected static final String EXEC_CHANNEL = "exec";

	public void openConnectionInternal() throws AuthenticationException, ConnectionException {
		if (authenticationInfo == null) {
			authenticationInfo = new AuthenticationInfo();
		}

		if (!interactive) {
			uIKeyboardInteractive = null;
			setInteractiveUserInfo(new NullInteractiveUserInfo());
		}

		SshConfiguration config = new SshConfiguration();
		ProxyInfo proxyInfo = getProxyInfo(ProxyInfo.PROXY_SOCKS5, getRepository().getHost());
		if (proxyInfo != null && proxyInfo.getHost() != null) {
			config.setProxyServer(new SshProxyServerDetails(SshProxyServerDetails.Type.SOCKS5, proxyInfo.getHost(),
					proxyInfo.getPort(), proxyInfo.getUserName(),
					proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
		} else {
			proxyInfo = getProxyInfo(ProxyInfo.PROXY_SOCKS4, getRepository().getHost());
			if (proxyInfo != null && proxyInfo.getHost() != null) {
				config.setProxyServer(new SshProxyServerDetails(SshProxyServerDetails.Type.SOCKS4, proxyInfo.getHost(),
						proxyInfo.getPort(), proxyInfo.getUserName(),
						proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
			} else {
				proxyInfo = getProxyInfo(ProxyInfo.PROXY_HTTP, getRepository().getHost());
				if (proxyInfo != null && proxyInfo.getHost() != null) {
					config.setProxyServer(new SshProxyServerDetails(SshProxyServerDetails.Type.HTTP,
							proxyInfo.getHost(), proxyInfo.getPort(), proxyInfo.getUserName(),
							proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
				} else {
					// Backwards compatibility
					proxyInfo = getProxyInfo(getRepository().getProtocol(), getRepository().getHost());
					if (proxyInfo != null && proxyInfo.getHost() != null) {
						// if port == 1080 we will use SOCKS5 Proxy, otherwise
						// will
						// use HTTP Proxy
						if (proxyInfo.getPort() == SOCKS5_PROXY_PORT) {
							config.setProxyServer(new SshProxyServerDetails(SshProxyServerDetails.Type.SOCKS5,
									proxyInfo.getHost(), proxyInfo.getPort(), proxyInfo.getUserName(),
									proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
						} else {
							config.setProxyServer(new SshProxyServerDetails(SshProxyServerDetails.Type.HTTP,
									proxyInfo.getHost(), proxyInfo.getPort(), proxyInfo.getUserName(),
									proxyInfo.getPassword() == null ? null : proxyInfo.getPassword().toCharArray()));
						}
					}
				}
			}
		}

		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		File tempHostKeyFile = null;

		// Bridge known hosts
		if (knownHostsProvider != null) {
			/*
			 * We need to write out to a temporary file as SSHAPI currently only supports
			 * real files for hosts file.
			 */
			try {
				tempHostKeyFile = File.createTempFile("hk", ".tmp");
				tempHostKeyFile.deleteOnExit();
				FileOutputStream fos = new FileOutputStream(tempHostKeyFile);
				try {
					fos.write(knownHostsProvider.getContents().getBytes("UTF-8"));
				} finally {
					fos.close();
				}
				config.getProperties().setProperty(SshConfiguration.CFG_KNOWN_HOSTS_PATH,
						tempHostKeyFile.getAbsolutePath());
			} catch (IOException ioe) {
				throw new ConnectionException("Could not configure known hosts file.", ioe);
			}

			try {
				SshHostKeyManager mgr = provider.createHostKeyManager(config);
				config.setHostKeyValidator(new PromptingKeyValidator(interactiveUserInfo, mgr));
			} catch (SshException sshe) {
				throw new ConnectionException("Failed to set key validator.", sshe);
			}
		} else {
			config.setHostKeyValidator(new ConsoleHostKeyValidator(null));
		}

		String authSock = System.getenv("SSH_AUTH_SOCK");
		if (provider.getCapabilities().contains(Capability.AGENT) && authSock != null && authSock.length() > 0) {
			try {
				agent = provider.connectToLocalAgent("Maven");
			} catch (SshException e) {
				e.printStackTrace();
			}
		}

		session = provider.createClient(config);

		session = config.createClient();

		File privateKey;
		try {
			privateKey = ScpHelper.getPrivateKey(authenticationInfo);
		} catch (FileNotFoundException e) {
			throw new AuthenticationException(e.getMessage());
		}

		// Connect

		String host = getRepository().getHost();
		int port = repository.getPort() == WagonConstants.UNKNOWN_PORT ? ScpHelper.DEFAULT_SSH_PORT
				: repository.getPort();
		try {
			String userName = authenticationInfo.getUserName();
			if (userName == null) {
				userName = System.getProperty("user.name");
			}
			session.connect(userName, host, port);
			try {
				session.setTimeout(getTimeout());
			} catch (Exception ioe) {
				//
				fireSessionError(ioe);
			}
		} catch (SshException e) {
			throw new AuthenticationException("Cannot connect. Reason: " + e.getMessage(), e);
		}

		// Connect the client to the agent
		if (agent != null)
			try {
				session.addChannelHandler(agent);
			} catch (SshException e1) {
				e1.printStackTrace();
				try {
					agent.close();
				} catch (IOException e) {
				}
				agent = null;
			}

		// Authenticate
		List authenticators = new ArrayList();

		if (agent != null) {
			fireSessionDebug("Using agent: " + agent);
			authenticators.add(new DefaultAgentAuthenticator(agent));
		}

		if (privateKey != null && privateKey.exists()) {
			fireSessionDebug("Using private key: " + privateKey);
			try {
				authenticators.add(new PEMFilePublicKeyAuthenticator(new ConsolePasswordAuthenticator(), privateKey));
			} catch (IOException e) {
				fireSessionError(e);
			}
		}

		if (interactiveUserInfo != null) {
			authenticators.add(new SshPasswordAuthenticator() {
				public char[] promptForPassword(SshClient session, String message) {
					String pw = interactiveUserInfo.promptPassword(message);
					return pw == null ? null : pw.toCharArray();
				}

				public String getTypeName() {
					return "password";
				}
			});
		}

		try {
			if (!session.authenticate((SshAuthenticator[]) authenticators.toArray(new SshAuthenticator[0]))) {
				throw new AuthenticationException("Cannot connect. Failed to authenticate.");
			}
		} catch (SshException e) {
			throw new ConnectionException("A exception occured during authentication.", e);
		}

		if (knownHostsProvider == null) {
			try {
				knownHostsProvider.storeKnownHosts(readFileAsString(tempHostKeyFile));
			} catch (IOException ioe) {
				fireSessionError(ioe);
			} finally {
				tempHostKeyFile.delete();
			}
		}
	}

	public void closeConnection() {
		if (session != null) {
			try {
				session.disconnect();
			} catch (SshException e) {
			}
			session = null;
		}
		if (agent != null) {
			try {
				agent.close();
			} catch (IOException e) {
			}
			agent = null;
		}
	}

	public Streams executeCommand(String command, boolean ignoreFailures) throws CommandExecutionException {
		SshCommand channel = null;
		BufferedReader stdoutReader = null;
		BufferedReader stderrReader = null;
		try {
			channel = session.createCommand(command + "\n");

			InputStream stdout = channel.getInputStream();
			InputStream stderr = channel.getExtendedInputStream();

			channel.open();

			stdoutReader = new BufferedReader(new InputStreamReader(stdout));
			stderrReader = new BufferedReader(new InputStreamReader(stderr));

			Streams streams = CommandExecutorStreamProcessor.processStreams(stderrReader, stdoutReader);

			if (streams.getErr().length() > 0 && !ignoreFailures) {
				int exitCode = channel.exitCode();
				throw new CommandExecutionException("Exit code: " + exitCode + " - " + streams.getErr());
			}

			return streams;
		} catch (SshException e) {
			throw new CommandExecutionException("Cannot execute remote command: " + command, e);
		} catch (IOException e) {
			throw new CommandExecutionException("Cannot execute remote command: " + command, e);
		} finally {
			IOUtil.close(stdoutReader);
			IOUtil.close(stderrReader);
			if (channel != null) {
				try {
					channel.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected void handleGetException(Resource resource, Exception e) throws TransferFailedException {
		fireTransferError(resource, e, TransferEvent.REQUEST_GET);

		String msg = "Error occurred while downloading '" + resource + "' from the remote repository:" + getRepository()
				+ ": " + e.getMessage();

		throw new TransferFailedException(msg, e);
	}

	public List getFileList(String destinationDirectory)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		return sshTool.getFileList(destinationDirectory, repository);
	}

	public void putDirectory(File sourceDirectory, String destinationDirectory)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		sshTool.putDirectory(this, sourceDirectory, destinationDirectory);
	}

	public boolean resourceExists(String resourceName) throws TransferFailedException, AuthorizationException {
		return sshTool.resourceExists(resourceName, repository);
	}

	public boolean supportsDirectoryCopy() {
		return true;
	}

	public void executeCommand(String command) throws CommandExecutionException {
		fireTransferDebug("Executing command: " + command);

		executeCommand(command, false);
	}

	/**
	 * Get the interactive user info.
	 * 
	 * @return interactive user info
	 */
	public InteractiveUserInfo getInteractiveUserInfo() {
		return this.interactiveUserInfo;
	}

	/**
	 * Get the known hosts provider.
	 * 
	 * @return know hosts provider
	 */
	public KnownHostsProvider getKnownHostsProvider() {
		return this.knownHostsProvider;
	}

	/**
	 * Set the interactive user info.
	 * 
	 * @param interactiveUserInfo
	 */
	public void setInteractiveUserInfo(InteractiveUserInfo interactiveUserInfo) {
		this.interactiveUserInfo = interactiveUserInfo;
	}

	public void setKnownHostsProvider(KnownHostsProvider knownHostsProvider) {
		this.knownHostsProvider = knownHostsProvider;
	}

	private static String readFileAsString(File file) throws java.io.IOException {
		byte[] buffer = new byte[(int) file.length()];
		FileInputStream f = new FileInputStream(file);
		f.read(buffer);
		return new String(buffer);
	}
}
