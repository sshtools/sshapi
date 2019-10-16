package net.sf.sshapi.impl.maverick16;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.sshtools.agent.KeyConstraints;
import com.sshtools.agent.client.AgentSocketType;
import com.sshtools.agent.client.SshAgentClient;
import com.sshtools.agent.exceptions.AgentNotAvailableException;
import com.sshtools.common.util.IOUtils;

import net.sf.sshapi.DefaultChannelData;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshChannel.ChannelData;
import net.sf.sshapi.SshChannelListener;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.identity.SshKeyPair;

public class MaverickAgent implements SshAgent {

	private SshAgentClient sshAgent;
	private String location;
	private AgentSocketType agentSocketType;

	public MaverickAgent(String application, String location, int socketType, int protocol) throws SshException {
		AgentSocketType agentSocketType = AgentSocketType.TCPIP;

		/* If location is blank, get if from environment variable */
		if (location == null || location.equals(""))
			location = System.getenv("SSH_AUTH_SOCK");

		/* If it's still blank, and we are on windows, assume to be the default */
		boolean windows = System.getProperty("os.name").toLowerCase().startsWith("windows");
		if ((location == null || location.equals("")) && windows)
			location = SshAgentClient.WINDOWS_SSH_AGENT_SERVICE;

		/* Detect socket type */
		if (socketType == SshAgent.AUTO_AGENT_SOCKET_TYPE) {
			if (!windows && location != null && !location.equals("")) {
				File fileLoc = new File(location);
				if (fileLoc.exists()) {
					/* Not on windows, standard file that exists, assume to be a domain socket */
					socketType = SshAgent.UNIX_DOMAIN_AGENT_SOCKET_TYPE;
				}
			} else if (windows && location != null && !location.equals("") && location.startsWith("\\")
					|| location.equals(SshAgentClient.WINDOWS_SSH_AGENT_SERVICE)) {
				/*
				 * Windows, and we do have a location starting with \, assume to be a named pipe
				 */
				socketType = SshAgent.NAMED_PIPED_AGENT_SOCKET_TYPE;
			} else {
				socketType = SshAgent.TCPIP_AGENT_SOCKET_TYPE;
			}
		}

		/*
		 * If have no location, and using TCPIP_AGENT_SOCKET_TYPE, then assume to be
		 * localhost. TODO: Do we need a port here?
		 */
		if ((location == null || location.equals("")) && socketType == SshAgent.TCPIP_AGENT_SOCKET_TYPE)
			location = "localhost";
		this.location = location;

		/* Get the native socket type */
		agentSocketType = getAgentSocketType(socketType);

		try {
			if (protocol == SshAgent.RFC_PROTOCOL)
				sshAgent = SshAgentClient.connectLocalAgent(application, location, agentSocketType, true);
			else
				/* NOTE: We can't actually auto-detect protocol at the moment */
				sshAgent = SshAgentClient.connectLocalAgent(application, location, agentSocketType, false);
		} catch (AgentNotAvailableException e) {
			throw new SshException(SshException.NO_AGENT, e);
		} catch (IOException e) {
			throw new SshException(SshException.FAILED_TO_CONNECT_TO_AGENT, e);
		}
	}

	private AgentSocketType getAgentSocketType(int socketType) {
		switch (socketType) {
		case SshAgent.UNIX_DOMAIN_AGENT_SOCKET_TYPE:
			return AgentSocketType.UNIX_DOMAIN;
		case SshAgent.NAMED_PIPED_AGENT_SOCKET_TYPE:
			return AgentSocketType.WINDOWS_NAMED_PIPE;
		default:
			return AgentSocketType.TCPIP;
		}
	}

	public SshAgentClient getAgent() {
		return sshAgent;
	}

	@Override
	public String[] getSupportChannelNames() {
		return new String[] { "auth-agent", "auth-agent@openssh.com" };
	}

	@Override
	public ChannelData createChannel(String channelName, byte[] requestData) {
		return new DefaultChannelData(32768, 32768, 0, requestData);
	}

	@Override
	public void channelCreated(SshChannel channel) throws IOException {

		try {
			final Socket socket = SshAgentClient.connectAgentSocket(location, agentSocketType);
			channel.addListener(new SshChannelListener<SshChannel>() {

				@Override
				public void opened(SshChannel ch) {
					final SshChannel channel = ch;
					Thread t = new Thread() {
						@Override
						public void run() {
							try {
								IOUtils.copy(socket.getInputStream(), channel.getOutputStream());
							} catch (IOException e) {
								SshConfiguration.getLogger().log(Level.ERROR, "I/O error during socket transfer", e);
								try {
									channel.close();
								} catch (IOException e1) {
								}
							}
						}
					};

					t.start();
				}
			});
			channel.addDataListener(new SshDataListener<SshChannel>() {

				@Override
				public void data(SshChannel ch, int direction, byte[] buf, int off, int len) {
					try {
						socket.getOutputStream().write(buf, off, len);
					} catch (IOException e) {
						SshConfiguration.getLogger().log(Level.ERROR, "I/O error during socket transfer", e);
						try {
							ch.close();
						} catch (IOException e1) {
						}
					}
				}
			});
		} catch (AgentNotAvailableException anae) {
			throw new IOException("Failed to create agent channel.", anae);
		}
	}

	@Override
	public void close() throws IOException {
		sshAgent.close();
	}

	@Override
	public void addKey(SshKeyPair keyPair, String description) throws SshException {
		KeyConstraints keyConstraints = new KeyConstraints();
		try {
			sshAgent.addKey(new MaverickSshPrivateKey(keyPair.getPrivateKey()),
					new MaverickSshPublicKey(keyPair.getPublicKey()), description, keyConstraints);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public Map<SshPublicKey, String> listKeys() throws SshException {
		try {
			Map<com.sshtools.common.ssh.components.SshPublicKey, String> nativeKeys = sshAgent.listKeys();
			Map<SshPublicKey, String> keys = new HashMap<>();
			for (Map.Entry<com.sshtools.common.ssh.components.SshPublicKey, String> en : nativeKeys.entrySet()) {
				try {
					keys.put(new MaverickPublicKey(en.getKey()), en.getValue());
				} catch (com.sshtools.common.ssh.SshException e) {
					throw new SshException(SshException.GENERAL, "Failed to convert key to SSHAPI.", e);
				}
			}
			return keys;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public boolean lockAgent(String password) throws SshException {
		try {
			return sshAgent.lockAgent(password);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public boolean unlockAgent(String password) throws SshException {
		try {
			return sshAgent.unlockAgent(password);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public byte[] getRandomData(int count) throws SshException {
		try {
			return sshAgent.getRandomData(count);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void deleteKey(SshPublicKey key, String description) throws SshException {
		try {
			sshAgent.deleteKey(new MaverickSshPublicKey(key), description);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public byte[] hashAndSign(SshPublicKey key, String algorithm, byte[] data) throws SshException {
		try {
			return sshAgent.hashAndSign(new MaverickSshPublicKey(key), algorithm, data);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void deleteAllKeys() throws SshException {
		try {
			sshAgent.deleteAllKeys();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public void ping(byte[] padding) throws SshException {
		try {
			sshAgent.ping(padding);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}
}
