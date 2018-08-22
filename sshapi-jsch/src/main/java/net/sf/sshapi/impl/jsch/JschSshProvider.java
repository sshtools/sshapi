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
package net.sf.sshapi.impl.jsch;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jcraft.jsch.JSch;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.util.Util;

/**
 * Provider implementation for JSch
 * 
 */
public class JschSshProvider extends AbstractProvider {
	/**
	 * Connection timeout for session
	 */
	public final static String CFG_SESSION_CONNECT_TIMEOUT = "sshapi.jsch.session.connectTimeout";
	/**
	 * Connection timeout for channel
	 */
	public final static String CFG_CHANNEL_CONNECT_TIMEOUT = "sshapi.jsch.channel.connectTimeout";
	protected static final SecureRandom RANDOM = new SecureRandom();
	//
	private static boolean firstConnection;

	/**
	 * Constructor
	 */
	public JschSshProvider() {
		super("JSch");
	}

	@Override
	public SshClient doCreateClient(SshConfiguration configuration) {
		// Much of JSch configuration is global :(
		configureAlgorithms(configuration);
		return new JschSshClient(configuration);
	}

	private void configureAlgorithms(SshConfiguration configuration) {
		JSch.setConfig("random", JschRandom.class.getName());
		String preferredClientToServerCipher = configuration.getPreferredClientToServerCipher();
		if (preferredClientToServerCipher != null) {
			checkConfig(preferredClientToServerCipher, "cipher", "cipher.c2s");
		}
		String preferredServerToClientCipher = configuration.getPreferredServerToClientCipher();
		if (preferredServerToClientCipher != null) {
			checkConfig(preferredServerToClientCipher, "cipher", "cipher.s2c");
		}
		String preferredClientToServerMAC = configuration.getPreferredClientToServerMAC();
		if (preferredClientToServerMAC != null) {
			checkConfig(preferredClientToServerMAC, "MAC", "mac.c2s");
		}
		String preferredServerToClientMAC = configuration.getPreferredServerToClientMAC();
		if (preferredServerToClientMAC != null) {
			checkConfig(preferredServerToClientMAC, "MAC", "mac.s2c");
		}
		String preferredClientToServerCompression = configuration.getPreferredClientToServerCompression();
		if (preferredClientToServerCompression != null) {
			checkConfig(preferredClientToServerCompression, "compression", "compression.c2s");
		}
		String preferredServerToClientCompression = configuration.getPreferredServerToClientCompression();
		if (preferredServerToClientCompression != null) {
			checkConfig(preferredServerToClientCompression, "compression", "compression.s2c");
		}
		String preferredKeyExchange = configuration.getPreferredKeyExchange();
		if (preferredKeyExchange != null) {
			checkConfig(preferredKeyExchange, "key exchange", "kex");
		}
		String preferredPublicKey = configuration.getPreferredPublicKey();
		if (preferredPublicKey != null) {
			checkConfig(preferredPublicKey, "public key", "server_host_key");
		}
	}

	@Override
	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.jcraft.jsch.JSch", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("JSch is not on the CLASSPATH");
		}
		if (configuration != null && configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("SSH1 is not supported.");
		}
	}

	@Override
	public List getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.SSH2, Capability.HTTP_PROXY, Capability.SOCKS4_PROXY,
				Capability.SOCKS5_PROXY, Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
				Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.IDENTITY_MANAGEMENT,
				Capability.HOST_KEY_MANAGEMENT, Capability.SFTP, Capability.SOCKET_FACTORY, Capability.WINDOW_CHANGE,
				Capability.TUNNELED_SOCKET_FACTORY, Capability.SCP, Capability.FILE_TRANSFER_EVENTS,
				Capability.DATA_TIMEOUTS, Capability.X11_FORWARDING, Capability.HOST_KEY_VERIFICATION,
				Capability.SHELL });
	}

	@Override
	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		return new JschHostKeyManager(configuration);
	}

	@Override
	public SshIdentityManager createIdentityManager(SshConfiguration configuration) {
		return new JschIdentityManager(configuration);
	}

	private void checkFirstConnection() {
		if (!firstConnection) {
			SshConfiguration.getLogger().log(Level.WARN,
					"JSch does not fully support per connection configuration. This second client's configuration "
							+ "may interfere with the first's.");
			firstConnection = true;
		}
	}

	private void checkConfig(String cipher, String name, String key) {
		checkFirstConnection();
		String[] split = JSch.getConfig(key).split(",");
		List ciphers = new ArrayList(Arrays.asList(split));
		ciphers.remove(cipher);
		ciphers.add(0, cipher);
		String delimited = Util.toDelimited((String[]) ciphers.toArray(new String[ciphers.size()]), ',');
		JSch.setConfig(key, delimited);
	}

	@Override
	public List getSupportedCiphers(int protocolVersion) {
		return Arrays.asList("aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-cbc,aes256-cbc".split(","));
	}

	@Override
	public List getSupportedCompression() {
		return Arrays.asList("zlib@openssh.com,zlib,none".split(","));
	}

	@Override
	public List getSupportedMAC() {
		return Arrays.asList("hmac-md5,hmac-sha1,hmac-sha1-96,hmac-md5-96".split(","));
	}

	@Override
	public List getSupportedKeyExchange() {
		return Arrays.asList(
				"diffie-hellman-group1-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha256"
						.split(","));
	}

	@Override
	public List getSupportedPublicKey() {
		return Arrays.asList("ssh-rsa,ssh-dss,ssh-ecdsa".split(","));
	}

	@Override
	public void seed(long seed) {
		// Jsch 'Random' interface is global anyway
		RANDOM.setSeed(seed);
	}
}
