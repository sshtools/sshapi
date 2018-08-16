package net.sf.sshapi.impl.maverick16;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maverick.agent.KeyConstraints;
import com.maverick.agent.client.AgentSocketType;
import com.maverick.agent.client.SshAgentClient;
import com.maverick.agent.exceptions.AgentNotAvailableException;
import com.maverick.util.IOUtil;

import net.sf.sshapi.DefaultChannelData;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshChannel.ChannelData;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshDataProducingComponent;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.identity.SshKeyPair;

public class MaverickAgent implements SshAgent {
	final static Logger LOG = LoggerFactory.getLogger(MaverickAgent.class);

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
			} else if (!windows && location != null && !location.equals("") && location.startsWith("\\")
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

	public String[] getSupportChannelNames() {
		return new String[] { "auth-agent", "auth-agent@openssh.com" };
	}

	public ChannelData createChannel(String channelName, byte[] requestData) {
		return new DefaultChannelData(32768, 32768, 0, requestData);
	}

	public void channelCreated(SshChannel channel) throws IOException {

		try {
			final Socket socket = SshAgentClient.connectAgentSocket(location, agentSocketType);
			channel.addListener(new SshLifecycleListener() {

				public void opened(SshLifecycleComponent ch) {
					final SshChannel channel = (SshChannel) ch;
					Thread t = new Thread() {
						public void run() {
							try {
								IOUtil.copy(socket.getInputStream(), channel.getOutputStream());
							} catch (IOException e) {
								LOG.error("I/O error during socket transfer", e);
								try {
									channel.close();
								} catch (SshException e1) {
								}
							}
						}
					};

					t.start();
				}

				public void closing(SshLifecycleComponent channel) {
				}

				public void closed(SshLifecycleComponent channel) {
				}
			});
			channel.addDataListener(new SshDataListener() {

				public void data(SshDataProducingComponent ch, int direction, byte[] buf, int off, int len) {
					final SshChannel channel = (SshChannel) ch;
					try {
						socket.getOutputStream().write(buf, off, len);
					} catch (IOException e) {
						LOG.error("I/O error during socket transfer", e);
						try {
							channel.close();
						} catch (SshException e1) {
						}
					}
				}
			});
		} catch (AgentNotAvailableException anae) {
			throw new IOException("Failed to create agent channel.", anae);
		}
	}

	public void close() throws IOException {
		sshAgent.close();
	}

	public void addKey(SshKeyPair keyPair, String description) throws SshException {
		KeyConstraints keyConstraints = new KeyConstraints();
		try {
			sshAgent.addKey(new MaverickSshPrivateKey(keyPair.getPrivateKey()),
					new MaverickSshPublicKey(keyPair.getPublicKey()), description, keyConstraints);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public Map listKeys() throws SshException {
		try {
			Map nativeKeys = sshAgent.listKeys();
			Map keys = new HashMap();
			for (Iterator it = nativeKeys.keySet().iterator(); it.hasNext();) {
				com.maverick.ssh.components.SshPublicKey pk = (com.maverick.ssh.components.SshPublicKey) it.next();
				try {
					keys.put(new MaverickPublicKey(pk), nativeKeys.get(pk));
				} catch (com.maverick.ssh.SshException e) {
					throw new SshException(SshException.GENERAL, "Failed to convert key to SSHAPI.", e);
				}
			}
			return keys;
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public boolean lockAgent(String password) throws SshException {
		try {
			return sshAgent.lockAgent(password);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public boolean unlockAgent(String password) throws SshException {
		try {
			return sshAgent.unlockAgent(password);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public byte[] getRandomData(int count) throws SshException {
		try {
			return sshAgent.getRandomData(count);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public void deleteKey(SshPublicKey key, String description) throws SshException {
		try {
			sshAgent.deleteKey(new MaverickSshPublicKey(key), description);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public byte[] hashAndSign(SshPublicKey key, byte[] data) throws SshException {
		try {
			return sshAgent.hashAndSign(new MaverickSshPublicKey(key), data);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public void deleteAllKeys() throws SshException {
		try {
			sshAgent.deleteAllKeys();
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	public void ping(byte[] padding) throws SshException {
		try {
			sshAgent.ping(padding);
		} catch (IOException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}
}
