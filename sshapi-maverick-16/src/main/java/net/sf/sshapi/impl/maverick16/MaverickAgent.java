package net.sf.sshapi.impl.maverick16;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maverick.agent.client.AgentSocketType;
import com.maverick.agent.client.SshAgentClient;
import com.maverick.agent.exceptions.AgentNotAvailableException;
import com.maverick.util.IOUtil;

import net.sf.sshapi.DefaultChannelData;
import net.sf.sshapi.SshChannel;
import net.sf.sshapi.SshDataListener;
import net.sf.sshapi.SshDataProducingComponent;
import net.sf.sshapi.SshChannel.ChannelData;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshLifecycleListener;
import net.sf.sshapi.agent.SshAgent;

public class MaverickAgent implements SshAgent {
	final static Logger LOG = LoggerFactory.getLogger(MaverickAgent.class);

	private SshAgentClient sshAgent;
	private String location;
	private AgentSocketType agentSocketType;

	public MaverickAgent(String application, String location, int socketType) throws SshException {
		AgentSocketType agentSocketType = AgentSocketType.TCPIP;
		this.location = location;
		if (socketType == UNIX_DOMAIN_AGENT_SOCKET_TYPE)
			agentSocketType = AgentSocketType.UNIX_DOMAIN;
		try {
			sshAgent = SshAgentClient.connectLocalAgent(application, location, agentSocketType);
		} catch (AgentNotAvailableException e) {
			throw new SshException(SshException.NO_AGENT, e);
		} catch (IOException e) {
			throw new SshException(SshException.FAILED_TO_CONNECT_TO_AGENT, e);
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

}
