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
package net.sf.sshapi.impl.libssh;

import java.util.Arrays;
import java.util.List;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKeyManager;
import ssh.SshLibrary;

public class LibsshSshProvider extends AbstractProvider {
	static {
		SshLibrary.INSTANCE.ssh_init();
	}

	public LibsshSshProvider() {
		super("libssh", "https://www.libssh.org");
		String ver = SshLibrary.INSTANCE.ssh_version(0);
		SshConfiguration.getLogger().info("libssh version %s", ver);
	}

	@Override
	public String getVersion() {
		return getVersion(getNumericVersion());
	}

	long getNumericVersion() {
		return getVersion(SshLibrary.INSTANCE.ssh_version(0));
	}

	String getVersion(long number) {
		long major = number % 10000;
		long minor = (number - major) % 100;
		long build = number - minor;
		return String.format("%d.%d.%d", major, minor, build);
	}

	long getVersion(String version) {
		String[] parts = version.split("/")[0].split("\\.");
		return (10000 * Integer.parseInt(parts[0])) + (100 * Integer.parseInt(parts[1])) + Integer.parseInt(parts[2]);
	}

	@Override
	protected SshClient doCreateClient(SshConfiguration configuration) {
		SshClient client = new LibsshClient(configuration);
		client.init(this);
		return client;
	}

	@Override
	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			SshLibrary.INSTANCE.hashCode();
		} catch (Exception e) {
			throw new UnsupportedOperationException("libssh is not installed");
		}
	}

	@Override
	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.PASSWORD_AUTHENTICATION,
				Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH2, Capability.SSH1, Capability.SCP,
				Capability.SFTP, Capability.FILE_TRANSFER_EVENTS, Capability.HOST_KEY_VERIFICATION, Capability.SHELL,
				Capability.KEYBOARD_INTERACTIVE_AUTHENTICATION, Capability.PUBLIC_KEY_AUTHENTICATION,
				Capability.SET_LAST_MODIFIED, Capability.RAW_SFTP, Capability.SFTP_READ_LINK,
				 /* Capability.LOCAL_PORT_FORWARD,Capability.REMOTE_PORT_FORWARD, */ Capability.FORWARDING_CHANNELS,
				Capability.TUNNELED_SOCKET_FACTORY, Capability.RECURSIVE_SCP_GET, Capability.SFTP_LSTAT, Capability.SFTP_RESUME,
				Capability.SFTP_OFFSET });
	}

	@Override
	public List<String> getSupportedCiphers(int protocolVersion) {
		return Arrays.asList(new String[] { "aes256-ctr", "aes192-ctr", "aes128-ctr", "aes256-cbc", "aes192-cbc",
				"aes128-cbc", "3des-cbc", "blowfish-cbc", "none" });
	}

	@Override
	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getSupportedCompression() {
		if (getNumericVersion() >= getVersion("0.8.0"))
			return Arrays.asList(new String[] { "zlib@openssh.com", "none" });
		else
			return Arrays.asList(new String[] { "zlib@openssh.com", "zlib", "none" });
	}

	@Override
	public List<String> getSupportedMAC() {
		if (getNumericVersion() >= getVersion("0.8.0"))
			return Arrays.asList(new String[] { "hmac-sha2-512", "hmac-sha2-256", "hmac-sha1", "none" });
		else
			return Arrays.asList(new String[] { "hmac-sha1", "none" });
	}

	@Override
	public List<String> getSupportedKeyExchange() {
		return Arrays.asList(new String[] { "curve25519-sha256@libssh.org", "ecdh-sha2-nistp256",
				"diffie-hellman-group1-sha1", "diffie-hellman-group14-sha1" });
	}

	@Override
	public List<String> getSupportedPublicKey() {
		return Arrays.asList(new String[] { SshConfiguration.PUBLIC_KEY_SSHRSA, SshConfiguration.PUBLIC_KEY_SSHDSA });
	}

	@Override
	public void seed(long seed) {
		// TODO seed libssh?
	}
}
