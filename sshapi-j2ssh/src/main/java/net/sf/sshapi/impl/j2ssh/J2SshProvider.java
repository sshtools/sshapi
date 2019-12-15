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
package net.sf.sshapi.impl.j2ssh;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.cipher.SshCipherFactory;
import com.sshtools.j2ssh.transport.compression.SshCompressionFactory;
import com.sshtools.j2ssh.transport.hmac.SshHmacFactory;
import com.sshtools.j2ssh.transport.kex.SshKeyExchangeFactory;

/**
 * Provider implementation for J2SSH.
 */
public class J2SshProvider extends AbstractProvider {
	
	static {
		if("false".equals(System.getProperty("sshapi.j2ssh.noWarning", "false"))) {
			System.err.println("J2SSH is currently unmaintained, and uses configuration now considered weak. If you really want to use this, "
					+ "it may be necessary to reconfigure your server and enable diffie-helman-group1-sha1 key exchange and blowfish-cbc and/or 3des-cbc ciphers. See https://unix.stackexchange.com/questions/340844/how-to-enable-diffie-hellman-group1-sha1-key-exchange-on-debian-8-0#340853");
		}
	}

	/**
	 * Constructor.
	 */
	public J2SshProvider() {
		super("J2SSH", "SSHTools");
	}

	@Override
	public String getVersion() {
		return Util.getArtifactVersion("sshtools", "j2ssh-core");
	}

	public SshClient doCreateClient(SshConfiguration configuration) {
		try {
			return new J2SshClient(configuration);
		} catch (SshException e) {
			throw new IllegalArgumentException("Could not create client for configuration.", e);
		}
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH2,
				Capability.HTTP_PROXY, Capability.SOCKS4_PROXY, Capability.SOCKS5_PROXY,
				Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
				Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.HOST_KEY_MANAGEMENT, Capability.SCP,
				Capability.SFTP, Capability.WINDOW_CHANGE, Capability.TUNNELED_SOCKET_FACTORY, Capability.DATA_TIMEOUTS,
				Capability.X11_FORWARDING, Capability.HOST_KEY_VERIFICATION, Capability.SHELL, Capability.SET_LAST_MODIFIED, Capability.LOCAL_PORT_FORWARD,
				Capability.REMOTE_PORT_FORWARD, Capability.FORWARDING_CHANNELS });
	}

	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws net.sf.sshapi.SshException {
		return new J2SshHostKeyManager(configuration);
	}

	@SuppressWarnings("unchecked")
	public List<String> getSupportedCiphers(int protocolVersion) {
		if (protocolVersion == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("Only SSH2 is supported by J2SSH");
		}
		return SshCipherFactory.getSupportedCiphers();
	}

	@SuppressWarnings("unchecked")
	public List<String> getSupportedCompression() {
		return SshCompressionFactory.getSupportedCompression();
	}

	@SuppressWarnings("unchecked")
	public List<String> getSupportedMAC() {
		return SshHmacFactory.getSupportedMacs();
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.sshtools.j2ssh.SshClient", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("J2SSH is not on the CLASSPATH");
		}
		if (configuration != null && configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("SSH1 is not supported.");
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getSupportedKeyExchange() {
		return SshKeyExchangeFactory.getSupportedKeyExchanges();
	}

	public List<String> getSupportedPublicKey() {
		return Arrays.asList(new String[] { "ssh-rsa", "ssh-dss" });
	}

	public void seed(long seed) {
		Random r = ConfigurationLoader.getRND();
		r.setSeed(seed);
	}
}
