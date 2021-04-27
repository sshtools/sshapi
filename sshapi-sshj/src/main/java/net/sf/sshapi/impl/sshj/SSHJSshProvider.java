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

	protected static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * Constructor
	 */
	public SSHJSshProvider() {
		super("SSHJ", "https://github.com/hierynomus/sshj");
	}

	@Override
	public String getVersion() {
		return Util.getArtifactVersion("com.hierynomus", "sshj");
	}

	@Override
	public SshClient doCreateClient(SshConfiguration configuration) {
		return new SSHJSshClient(configuration);
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
				Capability.SCP, Capability.FILTERS_SFTP_DOT_DIRECTORIES });
	}

	@Override
	public List<String> getSupportedCiphers(int protocolVersion) {
		// TODO
		return null;
	}

	@Override
	public List<String> getSupportedMAC() {
		// TODO
		return null;
	}

	@Override
	public List<String> getSupportedCompression() {
		// TODO
		return null;
	}

	@Override
	public List<String> getSupportedKeyExchange() {
		// TODO
		return null;
	}

	@Override
	public List<String> getSupportedPublicKey() {
		// TODO
		return null;
	}

	@Override
	public void seed(long seed) {
		RANDOM.setSeed(seed);
	}

}
