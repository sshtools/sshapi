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
package net.sf.sshapi.impl.ganymed;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import ch.ethz.ssh2.crypto.cipher.BlockCipherFactory;
import ch.ethz.ssh2.crypto.digest.MAC;
import ch.ethz.ssh2.transport.KexManager;
import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

/**
 * Provider implementation for Ganymed
 */
public class GanymedSshProvider extends AbstractProvider {
	/**
	 * Single X11 connection
	 */
	private SecureRandom rng;

	/**
	 * Constructor.
	 */
	public GanymedSshProvider() {
		super("Ganymed", "http://www.ganymed.ethz.ch/ssh2");
		rng = new SecureRandom();
	}

	@Override
	public String getVersion() {
		return Util.getArtifactVersion("ch.ethz.ganymed", "ganymed-ssh2");
	}

	protected SshClient doCreateClient(SshConfiguration configuration) {
		SshClient client = new GanymedSshClient(configuration, rng);
		client.init(this);
		return client;
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("ch.ethz.ssh2.Connection", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			SshConfiguration.getLogger().debug("Could not find Ganymed class ch.ethz.ssh2.Connection.");
			throw new UnsupportedOperationException("Ganymed is not on the CLASSPATH");
		}
		if (configuration != null && configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			SshConfiguration.getLogger().debug("Ganymed does not support SSH1, not usable.");
			throw new UnsupportedOperationException("SSH1 is not supported.");
		}
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
				Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH2,
				Capability.HTTP_PROXY, Capability.HOST_KEY_MANAGEMENT, Capability.SCP, Capability.SFTP,
				Capability.TUNNELED_SOCKET_FACTORY, Capability.X11_FORWARDING, Capability.X11_FORWARDING_TCP, Capability.HOST_KEY_VERIFICATION,
				Capability.RAW_SFTP, Capability.SET_LAST_MODIFIED, Capability.LOCAL_PORT_FORWARD,
				Capability.REMOTE_PORT_FORWARD, Capability.SFTP_READ_LINK, Capability.FORWARDING_CHANNELS });
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		if (protocolVersion == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("Only SSH2 is supported by Ganymed");
		}
		return Arrays.asList(BlockCipherFactory.getDefaultCipherList());
	}

	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		return new GanymedHostKeyManager(configuration);
	}

	public List<String> getSupportedCompression() {
		return Arrays.asList("none");
	}

	public List<String> getSupportedMAC() {
		return Arrays.asList(MAC.getMacList());
	}

	public List<String> getSupportedKeyExchange() {
		return Arrays.asList(KexManager.getDefaultClientKexAlgorithmList());
	}

	public List<String> getSupportedPublicKey() {
		return Arrays.asList(KexManager.getDefaultServerHostkeyAlgorithmList());
	}

	public void seed(long seed) {
		rng.setSeed(seed);
	}
}
