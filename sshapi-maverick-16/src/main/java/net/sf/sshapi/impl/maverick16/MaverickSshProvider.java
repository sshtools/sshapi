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
package net.sf.sshapi.impl.maverick16;

import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.identity.SshIdentityManager;

import com.maverick.ssh.SshConnector;
import com.maverick.ssh.SshException;
import com.maverick.ssh.components.jce.JCEProvider;
import com.maverick.ssh2.Ssh2Context;

/**
 * Provider implementation for Maverick SSH.
 */
public class MaverickSshProvider extends AbstractProvider {
	/**
	 * The User-Agent Maverick identifies itself as when using HTTP proxy
	 * connection
	 */
	public final static String CFG_HTTP_PROXY_USER_AGENT = "sshapi.maverick.httpProxy.userAgent";

	/**
	 * Look up proxied hostnames locally
	 */
	public static final String CFG_SOCKS5_PROXY_LOCAL_LOOKUP = "sshapi.maverick.socks5Proxy.localLookup";

	/**
	 * SFTP mode, can be one of SFTP_ALL_MODES, SFTP_OVER_SCP or SFTP_SUBSYSTEM
	 */
	public static final String CFG_SFTP_MODE = "sshapi.maverick.sftp.mode";

	/**
	 * SFTP protocol maximum version (defaults to the highest supported by
	 * Maverick)
	 */
	public static final String CFG_SFTP_MAX_VERSION = "sshapi.maverick.sftp.maxVersion";


	private final static Capability[] DEFAULT_CAPS = new Capability[] { Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH1,
			Capability.SSH2, Capability.HTTP_PROXY, Capability.SOCKS4_PROXY, Capability.SOCKS5_PROXY,
			Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
			Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.GSSAPI_AUTHENTICATION,
			Capability.HOST_KEY_MANAGEMENT, Capability.IDENTITY_MANAGEMENT, Capability.PORT_FORWARD_EVENTS,
			Capability.CHANNEL_DATA_EVENTS, Capability.SCP, Capability.SFTP, Capability.PUBLIC_KEY_SUBSYSTEM,
			Capability.SOCKET_FACTORY, Capability.WINDOW_CHANGE, Capability.TUNNELED_SOCKET_FACTORY,
			Capability.SFTP_OVER_SCP, Capability.FILE_TRANSFER_EVENTS, Capability.DATA_TIMEOUTS,
			Capability.CHANNEL_HANDLERS };
	
	private SshConnector con;

	static {
		// Warning for slow startup on Linux / Solaris
		if ((System.getProperty("os.name").toLowerCase().indexOf("linux") != -1
				|| System.getProperty("os.name").toLowerCase().indexOf("solaris") != -1)
				&& System.getProperty("java.security.egd") == null) {
			SshConfiguration.getLogger().log(Level.WARN,
					"If you experience slow startup of the Maverick API on Linux or Solaris, try setting the system property java.security.egd=file:/dev/urandom");
		}

	}

	private synchronized void checkConnector() {
		if (con == null) {
			/*
			 * Use reflection to keep compatibility between version 1.6 and 1.7.
			 * This is the only incompatibile API change (that we care about)
			 * between the two versions
			 */
			try {
				try {
					Method m = SshConnector.class.getMethod("getInstance", new Class[] {});
					con = (SshConnector) m.invoke(null, new Object[0]);
				} catch (NoSuchMethodException e) {
					Method m = SshConnector.class.getMethod("createInstance", new Class[] {});
					con = (SshConnector) m.invoke(null, new Object[0]);
				}
			} catch (Exception e2) {
				throw new RuntimeException("Failed to create connector.", e2);
			}
		}
	}

	/**
	 * Constructor
	 */
	public MaverickSshProvider() {
		super("Maverick 1.6+");
	}

	public SshIdentityManager createIdentityManager(SshConfiguration configurshation) {
		return new MaverickIdentityManager();
	}

	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws net.sf.sshapi.SshException {
		return new MaverickHostKeyManager(configuration);
	}

	public SshClient doCreateClient(SshConfiguration configuration) {
		checkConnector();
		try {
			return new MaverickSshClient(con, configuration);
		} catch (SshException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		}
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.maverick.ssh.SshConnector", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("Maverick is not on the CLASSPATH");
		}
	}

	public List getCapabilities() {
		List caps = Arrays.asList(DEFAULT_CAPS);
		try {
			Class.forName("com.maverick.agent.client.SshAgentClient");
			caps = new ArrayList(caps);
			caps.add(Capability.AGENT);
			caps.add(Capability.OPENSSH_AGENT);
			caps.add(Capability.RFC_AGENT);
			caps = Collections.unmodifiableList(caps);
		} catch (ClassNotFoundException cnfe) {
		}
		return caps;
	}

	public List getSupportedCiphers(int protocolVersion) {
		checkConnector();
		List ciphers = new ArrayList();
		if (protocolVersion == SshConfiguration.SSH1_OR_SSH2 || protocolVersion == SshConfiguration.SSH2_ONLY) {
			Ssh2Context ssh2Context;
			try {
				ssh2Context = (Ssh2Context) con.getContext(SshConnector.SSH2);
			} catch (SshException e) {
				throw new UnsupportedOperationException(e);
			}
			ciphers.addAll(Arrays.asList(ssh2Context.supportedCiphersCS().list("").split(",")));
		}
		if (protocolVersion == SshConfiguration.SSH1_OR_SSH2 || protocolVersion == SshConfiguration.SSH1_ONLY) {
			ciphers.add(SshConfiguration.CIPHER_3DES);
			ciphers.add(SshConfiguration.CIPHER_DES);
		}
		return ciphers;
	}

	public List getSupportedCompression() {
		checkConnector();
		List compressions = new ArrayList();
		try {
			Ssh2Context ssh2Context = (Ssh2Context) con.getContext(SshConnector.SSH2);
			compressions.addAll(Arrays.asList(ssh2Context.supportedCompressionsCS().list("").split(",")));
		} catch (SshException e) {
			throw new UnsupportedOperationException(e);
		}
		return compressions;
	}

	public List getSupportedMAC() {
		checkConnector();
		List macs = new ArrayList();
		try {
			Ssh2Context ssh2Context = (Ssh2Context) con.getContext(SshConnector.SSH2);
			macs.addAll(Arrays.asList(ssh2Context.supportedMacsCS().list("").split(",")));
		} catch (SshException e) {
			throw new UnsupportedOperationException(e);
		}
		return macs;
	}

	public List getSupportedKeyExchange() {
		checkConnector();
		List kexs = new ArrayList();
		try {
			Ssh2Context ssh2Context = (Ssh2Context) con.getContext(SshConnector.SSH2);
			kexs.addAll(Arrays.asList(ssh2Context.supportedKeyExchanges().list("").split(",")));
		} catch (SshException e) {
			throw new UnsupportedOperationException(e);
		}
		return kexs;
	}

	public List getSupportedPublicKey() {
		checkConnector();
		List pks = new ArrayList();
		try {
			Ssh2Context ssh2Context = (Ssh2Context) con.getContext(SshConnector.SSH2);
			pks.addAll(Arrays.asList(ssh2Context.supportedPublicKeys().list("").split(",")));
		} catch (SshException e) {
			throw new UnsupportedOperationException(e);
		}
		return pks;
	}

	public void seed(long seed) {
		SecureRandom rnd;
		try {
			rnd = JCEProvider.getSecureRandom();
			rnd.setSeed(seed);
		} catch (NoSuchAlgorithmException e) {
			SshConfiguration.getLogger().log(Level.ERROR, "Failed to set seed.", e);
		}
	}

	public SshAgent connectToLocalAgent(String application, int protocol)
			throws net.sf.sshapi.SshException {
		return new MaverickAgent(application, null, SshAgent.AUTO_AGENT_SOCKET_TYPE, protocol);
	}

	public SshAgent connectToLocalAgent(String application, String location, int socketType, int protocol)
			throws net.sf.sshapi.SshException {
		return new MaverickAgent(application, location, socketType, protocol);
	}
}
