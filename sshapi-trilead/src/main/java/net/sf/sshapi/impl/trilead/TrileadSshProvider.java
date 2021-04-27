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
package net.sf.sshapi.impl.trilead;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import com.trilead.ssh2.crypto.cipher.BlockCipherFactory;
import com.trilead.ssh2.crypto.digest.MAC;
import com.trilead.ssh2.transport.KexManager;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import net.sf.sshapi.util.Util;

/**
 * Provider implementation for Trilead
 */
public class TrileadSshProvider extends AbstractProvider {
	/**
	 * Single X11 connection
	 */
	private SecureRandom rng;

	/**
	 * Constructor.
	 */
	public TrileadSshProvider() {
		super("Trilead", "https://github.com/jenkinsci/trilead-ssh2");
		rng = new SecureRandom();
	}

	@Override
	public String getVersion() {
		return Util.getManifestVersion(TrileadSshProvider.class, "Build-Version");
	}

	protected SshClient doCreateClient(SshConfiguration configuration) {
		SshClient client = new TrileadSshClient(configuration, rng);
		client.init(this);
		return client;
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("com.trilead.ssh2.Connection", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			SshConfiguration.getLogger().debug("Could not find Trilead class com.trilead.ssh2.Connection.");
			throw new UnsupportedOperationException("Trilead is not on the CLASSPATH");
		}
		if (configuration != null && configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			SshConfiguration.getLogger().debug("Trilead does not support SSH1, not usable.");
			throw new UnsupportedOperationException("SSH1 is not supported.");
		}
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.PASSWORD_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
				Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH2,
				Capability.HTTP_PROXY, Capability.HOST_KEY_MANAGEMENT, Capability.SCP, Capability.SFTP,
				Capability.TUNNELED_SOCKET_FACTORY, Capability.HOST_KEY_VERIFICATION, Capability.SHELL,
				Capability.RAW_SFTP, Capability.SET_LAST_MODIFIED, Capability.LOCAL_PORT_FORWARD,
				Capability.REMOTE_PORT_FORWARD, Capability.SFTP_READ_LINK, Capability.FORWARDING_CHANNELS, 
				Capability.SFTP_LSTAT, Capability.SFTP_OFFSET, Capability.SFTP_RESUME });
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		if (protocolVersion == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("Only SSH2 is supported by Trilead");
		}
		return Arrays.asList(BlockCipherFactory.getDefaultCipherList());
	}

	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		return new TriliadHostKeyManager(configuration);
	}

	public List<String> getSupportedCompression() {
		return Arrays.asList("none");
	}

	public List<String> getSupportedMAC() {
		return Arrays.asList(MAC.getMacList());
	}

	public List<String> getSupportedKeyExchange() {
		return Arrays.asList(KexManager.getDefaultKexAlgorithmList());
	}

	public List<String> getSupportedPublicKey() {
		return Arrays.asList(KexManager.getDefaultServerHostkeyAlgorithmList());
	}

	public void seed(long seed) {
		rng.setSeed(seed);
	}
}
