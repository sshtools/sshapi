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
package net.sf.sshapi.impl.sshj;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.transport.random.Random;
import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.util.Util;

/**
 * Provider implementation for SSHJ
 * 
 */
public class SSHJSshProvider extends AbstractProvider {
	/**
	 * The maximum number of concurrent sockets that can be accepted by the client's
	 * server socket used in a local port forward.
	 */
	public final static String CFG_LOCAL_FORWARD_BACKLOG = "sshapi.sshj.localForwardBacklog";

	private DefaultConfig underlyingConfiguration;
	private long seed;

	/**
	 * Constructor
	 */
	public SSHJSshProvider() {
		super("SSHJ", "https://github.com/hierynomus/sshj");

		underlyingConfiguration = new DefaultConfig();
		underlyingConfiguration.setRandomFactory(() -> new Random() {
			private byte[] tmp = new byte[16];
			protected final SecureRandom random = new SecureRandom();

			{
				random.setSeed(seed);
			}

			@Override
			public synchronized void fill(byte[] foo, int start, int len) {
				if (start == 0 && len == foo.length) {
					random.nextBytes(foo);
				} else {
					synchronized (this) {
						if (len > tmp.length)
							tmp = new byte[len];
						random.nextBytes(tmp);
						System.arraycopy(tmp, 0, foo, start, len);
					}
				}
			}

			@Override
			public void fill(final byte[] bytes) {
				random.nextBytes(bytes);
			}

		});
	}

	@Override
	public String getVersion() {
		return Util.getArtifactVersion("com.hierynomus", "sshj");
	}

	@Override
	public SshClient doCreateClient(SshConfiguration configuration) {
		return new SSHJSshClient(this.underlyingConfiguration, configuration);
	}

	@Override
	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			Class.forName("net.schmizz.sshj.SSHClient", false, getClass().getClassLoader());
		} catch (ClassNotFoundException cnfe) {
			throw new UnsupportedOperationException("SSHJ is not on the CLASSPATH");
		}
		if (configuration != null && configuration.getProtocolVersion() == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("SSH1 is not supported.");
		}
	}

	@Override
	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.SSH2, Capability.PASSWORD_AUTHENTICATION, Capability.SHELL,
				Capability.SET_LAST_MODIFIED, Capability.SFTP, Capability.SFTP_LSTAT, Capability.SFTP_READ_LINK,
				Capability.SCP, Capability.FILTERS_SFTP_DOT_DIRECTORIES, Capability.LOCAL_PORT_FORWARD, Capability.REMOTE_PORT_FORWARD,
				Capability.RECURSIVE_SCP_GET, Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION });
	}

	@Override
	public List<String> getSupportedCiphers(int protocolVersion) {
		return underlyingConfiguration.getCipherFactories().stream().map(k -> k.getName()).collect(Collectors.toList());
	}

	@Override
	public List<String> getSupportedMAC() {
		return underlyingConfiguration.getMACFactories().stream().map(k -> k.getName()).collect(Collectors.toList());
	}

	@Override
	public List<String> getSupportedCompression() {
		return underlyingConfiguration.getCompressionFactories().stream().map(k -> k.getName())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getSupportedKeyExchange() {
		return underlyingConfiguration.getKeyExchangeFactories().stream().map(k -> k.getName())
				.collect(Collectors.toList());
	}

	@Override
	public List<String> getSupportedPublicKey() {
		return underlyingConfiguration.getKeyAlgorithms().stream().map(k -> k.getName()).collect(Collectors.toList());
	}

	@Override
	public void seed(long seed) {
		this.seed = seed;
	}

}
