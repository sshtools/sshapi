/**
 * Copyright (c) 2020 The JavaSSH Project
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.sf.sshapi.impl.mavericksynergy;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sshtools.client.SshClientContext;
import com.sshtools.client.components.Rsa1024Sha1;
import com.sshtools.client.components.Rsa2048Sha256;
import com.sshtools.common.logger.Log;
import com.sshtools.common.logger.Log.Level;
import com.sshtools.common.logger.RootLoggerContext;
import com.sshtools.common.ssh.SecurityLevel;
import com.sshtools.common.ssh.SshException;
import com.sshtools.common.ssh.components.ComponentFactory;
import com.sshtools.common.ssh.components.ComponentManager;
import com.sshtools.common.ssh.components.jce.JCEComponentManager;
import com.sshtools.common.ssh.components.jce.JCEProvider;
import com.sshtools.common.ssh.compression.NoneCompression;
import com.sshtools.common.ssh.compression.SshCompression;
import com.sshtools.synergy.nio.SshEngine;
import com.sshtools.synergy.ssh.SshContext;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.util.ConsoleLogger;
import net.sf.sshapi.util.Util;

/**
 * Provider implementation for Maverick SSH.
 */
public class MaverickSynergySshProvider extends AbstractProvider {
	private final static Capability[] DEFAULT_CAPS = new Capability[] { Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH2,
			Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
			Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.IDENTITY_MANAGEMENT, Capability.SFTP,
			Capability.WINDOW_CHANGE, Capability.FILE_TRANSFER_EVENTS, Capability.IO_TIMEOUTS,
			Capability.HOST_KEY_VERIFICATION, Capability.HOST_KEY_MANAGEMENT, Capability.SHELL,
			Capability.RAW_SFTP, Capability.SFTP_TRANSFER_MODE, Capability.SET_LAST_MODIFIED, Capability.LOCAL_PORT_FORWARD,
			Capability.REMOTE_PORT_FORWARD, Capability.SFTP_READ_LINK, Capability.FORWARDING_CHANNELS,
			Capability.TUNNELED_SOCKET_FACTORY, Capability.SFTP_LSTAT, Capability.SFTP_RESUME, Capability.SFTP_OFFSET,
			Capability.SCP, Capability.RECURSIVE_SCP_GET, Capability.CHANNEL_DATA_EVENTS, Capability.PORT_FORWARD_EVENTS,
			Capability.X11_FORWARDING, Capability.X11_FORWARDING_UNIX_SOCKET, Capability.SFTP_HARD_LINK,
			Capability.FORCE_KEY_EXCHANGE };
 
			/**
			 * SFTP mode, can be one of SFTP_ALL_MODES, SFTP_OVER_SCP or SFTP_SUBSYSTEM
			 */
	public static final String CFG_AUTHENTICATE_TIMEOUT = "sshapi.maverickSynergy.authenticationTimeout";;
	public static final long CFG_AUTHENTICATE_TIMEOUT_DEFAULT = 60000;
	
	private SshEngine engine;
	private ComponentManager componentManager;
	static {
		// Warning for slow startup on Linux / Solaris
		if ((System.getProperty("os.name").toLowerCase().indexOf("linux") != -1
				|| System.getProperty("os.name").toLowerCase().indexOf("solaris") != -1)
				&& System.getProperty("java.security.egd") == null) {
			SshConfiguration.getLogger().warn(
					"If you experience slow startup of the Maverick API on Linux or Solaris, try setting the system property java.security.egd=file:/dev/urandom");
		}
		ComponentManager.enableCBCCiphers();
		Log.setDefaultContext(new RootLoggerContext() {
			@Override
			public void raw(com.sshtools.common.logger.Log.Level level, String msg) {
				SshConfiguration.getLogger().raw(toLevel(level), msg);
			}
			
			@Override
			public void newline() {
				SshConfiguration.getLogger().newline();
			}
			
			@Override
			public void log(com.sshtools.common.logger.Log.Level level, String msg, Throwable e, Object... args) {
				try {
					int p = 0;
					while(true) {
						int idx = msg.indexOf("{}");
						if(idx == -1)
							break;
						msg = msg.replaceFirst("\\{\\}", "{" + (p++) + "}");
					}
					SshConfiguration.getLogger().log(toLevel(level), msg, e, args);
				}
				catch(Exception ex) {
					SshConfiguration.getLogger().log(toLevel(level), msg + " " + Arrays.asList(args), e);
				}
			}

			@Override
			public void enableFile(Level level, String logFile) {
				enableFile(level, new File(logFile));
			}
			
			@Override
			public synchronized void enableFile(Level level, File logFile) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public synchronized void enableFile(Level level, File logFile, int maxFiles, long maxSize) {
				throw new UnsupportedOperationException();
			}
			
			private net.sf.sshapi.Logger.Level toLevel(com.sshtools.common.logger.Log.Level level) {
				switch(level) {
				case DEBUG:
					return net.sf.sshapi.Logger.Level.DEBUG;
				case INFO:
					return net.sf.sshapi.Logger.Level.INFO;
				case WARN:
					return net.sf.sshapi.Logger.Level.WARN;
				case ERROR:
					return net.sf.sshapi.Logger.Level.ERROR;
				default:
					return net.sf.sshapi.Logger.Level.TRACE;
				}
			}

			@Override
			public boolean isLogging(com.sshtools.common.logger.Log.Level level) {
				return SshConfiguration.getLogger().isLevelEnabled(toLevel(level));
			}
			
			@Override
			public void close() {
			}

			@Override
			public void enableConsole(Level level) {
				if(SshConfiguration.getLogger() instanceof ConsoleLogger)
					((ConsoleLogger)SshConfiguration.getLogger()).setDefaultLevel(toLevel(level));
			}

			@Override
			public String getProperty(String key, String defaultValue) {
				return defaultValue;
			}

			@Override
			public void shutdown() {
			}
		});
	}

	private synchronized void checkEngine() {
		if (engine == null) {
			
			/*
			 * Check for BouncyCastle -
			 * https://www.jadaptive.com/app/manpage/en/article/1570724/Using-
			 * BouncyCastle-with-the-Synergy-API
			 */
			try {
				Class.forName("com.sshtools.common.publickey.OpenSSHPrivateKeyFileBC");
				/* Standard bouncycastle */
				JCEProvider.enableBouncyCastle(true);
			} catch (Exception e) {
				try {
					Class.forName("com.sshtools.common.publickey.OpenSSHPrivateKeyFileBCFIPS");
					/* FIPS mode bouncycastle */
					JCEProvider.enableBouncyCastle(true);
				} catch (Exception e2) {
					/* Using JCE */
				}
			}
			engine = new SshEngine();
			try {
				engine.startup();
			} catch (IOException e) {
				throw new IllegalStateException("I/O error starting Maverick NG SSH Engine");
			}
		}
	}

	/**
	 * Constructor
	 */
	public MaverickSynergySshProvider() {
		super("Maverick Synergy", "JADAPTIVE");
		componentManager = ComponentManager.getDefaultInstance();
	}

	@Override
	public String getVersion() {
		return Util.getArtifactVersion("com.sshtools", "maverick-synergy-client");
	}

	/**
	 * Get the native engine.
	 * 
	 * @return engine
	 */
	public SshEngine getEngine() {
		checkEngine();
		return engine;
	}

	public SshIdentityManager createIdentityManager(SshConfiguration configurshation) {
		return new MaverickSynergyIdentityManager();
	}

	public SshClient doCreateClient(SshConfiguration configuration) {
		checkEngine();
		try {
			return new MaverickSynergySshClient(configuration);
		} catch (SshException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		}
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.sshtools.client.SshClient", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("Maverick is not on the CLASSPATH");
		}
	}

	@Override
	public List<Capability> getCapabilities() {
		List<Capability> caps = Arrays.asList(DEFAULT_CAPS);
		try {
			Class.forName("com.sshtools.agent.client.SshAgentClient");
			caps = new ArrayList<>(caps);
			caps.add(Capability.AGENT);
			caps.add(Capability.OPENSSH_AGENT);
			caps.add(Capability.RFC_AGENT);
			caps = Collections.unmodifiableList(caps);
		} catch (ClassNotFoundException cnfe) {
		}
		try {
			Class.forName("com.sshtools.common.ssh.components.X509Helper");
			caps = new ArrayList<>(caps);
			caps.add(Capability.X509_PUBLIC_KEY);
			caps = Collections.unmodifiableList(caps);
		} catch (ClassNotFoundException cnfe) {
		}
		return caps;
	}

	@Override
	public List<String> getFingerprintHashingAlgorithms() {
		return Arrays.asList(SshConfiguration.FINGERPRINT_MD5, SshConfiguration.FINGERPRINT_SHA1,
				SshConfiguration.FINGERPRINT_SHA256);
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		checkEngine();
		return Arrays.asList(componentManager.supportedSsh2CiphersCS().list("").split(","));
	}

	@Override
	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws net.sf.sshapi.SshException {
		return new MaverickSynergyHostKeyManager(configuration);
	}

	public List<String> getSupportedCompression() {
		checkEngine();
		ComponentFactory<SshCompression> compressionsCS = new ComponentFactory<SshCompression>(componentManager);
		compressionsCS.add(SshContext.COMPRESSION_NONE, NoneCompression.class);
		JCEComponentManager.getDefaultInstance().loadExternalComponents("zip.properties", compressionsCS);
		return Arrays.asList(compressionsCS.list("").split(","));
	}

	public List<String> getSupportedMAC() {
		checkEngine();
		return Arrays.asList(componentManager.supportedHMacsCS().list("").split(","));
	}

	public List<String> getSupportedKeyExchange() {
		checkEngine();
		try {
			SshClientContext ctx = new SshClientContext(engine, componentManager, SecurityLevel.WEAK);
			return Arrays.asList(ctx.supportedKeyExchanges().list("").split(","));
		} catch (IOException | SshException e) {
			return Arrays.asList("diffie-hellman-group-exchange-sha256",
					"diffie-hellman-group14-sha256", 
					"diffie-hellman-group15-sha512", 
					"diffie-hellman-group16-sha512", 
					"diffie-hellman-group17-sha512", 
					"diffie-hellman-group18-sha512", 
					"diffie-hellman-group14-sha1", 
					"ecdh-sha2-nistp256", 
					"ecdh-sha2-nistp384", 
					"ecdh-sha2-nistp521", 
					Rsa2048Sha256.RSA_2048_SHA256,
					"diffie-hellman-group-exchange-sha1",
					"diffie-hellman-group1-sha1",
					Rsa1024Sha1.RSA_1024_SHA1
					);
		}
	}

	public List<String> getSupportedPublicKey() {
		checkEngine();
		List<String> pks = new ArrayList<>();
		pks.addAll(Arrays.asList(componentManager.supportedPublicKeys().list("").split(",")));
		return pks;
	}

	public void seed(long seed) {
		SecureRandom rnd = JCEProvider.getSecureRandom();
		rnd.setSeed(seed);
	}

	@Override
	public SshAgent connectToLocalAgent(String application, int protocol) throws net.sf.sshapi.SshException {
		return new MaverickSynergyAgent(application, null, SshAgent.AUTO_AGENT_SOCKET_TYPE, protocol);
	}

	@Override
	public SshAgent connectToLocalAgent(String application, String location, int socketType, int protocol)
			throws net.sf.sshapi.SshException {
		return new MaverickSynergyAgent(application, location, socketType, protocol);
	}
}
