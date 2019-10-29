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
package net.sf.sshapi.impl.openssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;

/**
 * Provider implementation for Maverick SSH.
 */
public class OpenSshProvider extends AbstractProvider {
	/**
	 * Command to use for 'ssh'. By default, this is just 'ssh', but a full path
	 * may be specified.
	 */
	public final static String CFG_OPENSSH_SSH_COMMAND = "sshapi.openssh.sshCommand";
	/**
	 * Command to use for 'ssh'. By default, this is just 'sftp', but a full
	 * path may be specified.
	 */
	public final static String CFG_OPENSSH_SFTP_COMMAND = "sshapi.openssh.sftpCommand";
	/**
	 * Command to use for 'ssh'. By default, this is just 'scp', but a full path
	 * may be specified.
	 */
	public final static String CFG_OPENSSH_SCP_COMMAND = "sshapi.openssh.scpCommand";
	/**
	 * Command to use for 'ssh-keygen'. By default, this is just 'ssh-keygen', but a full path
	 * may be specified.
	 */
	public final static String CFG_OPENSSH_SSH_KEYGEN_COMMAND = "sshapi.openssh.sshKeyGenCommand";
	
	private final static Capability[] DEFAULT_CAPS = new Capability[] { Capability.PER_CONNECTION_CONFIGURATION, Capability.SSH1,
			Capability.SSH2, Capability.PASSWORD_AUTHENTICATION, Capability.SFTP,
			Capability.PUBLIC_KEY_AUTHENTICATION, Capability.SHELL, Capability.SCP, Capability.LOCAL_PORT_FORWARD, Capability.RECURSIVE_SCP_GET
			 };

	/**
	 * Constructor
	 */
	public OpenSshProvider() {
		super("OpenSSH");
	}

	public SshClient doCreateClient(SshConfiguration configuration) {
		return new OpenSshClient(configuration);
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		// try {
		// Class.forName("com.sshtools.client.SshClient", false,
		// getClass().getClassLoader());
		// } catch (ClassNotFoundException cnfe) {
		// throw new UnsupportedOperationException("Maverick is not on the
		// CLASSPATH");
		// }
	}

	@Override
	public String getVersion() {
		ProcessBuilder b = new ProcessBuilder("ssh", "-V");
		try {
			b.redirectErrorStream(true);
			Process p = b.start();
			try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				return r.readLine().split("\\s+")[0];
			} finally {
				p.waitFor();
			}
		} catch (InterruptedException ie) {
			return super.getVersion();
		} catch (IOException ioe) {
			return super.getVersion();
		}
	}

	@Override
	public String getVendor() {
		ProcessBuilder b = new ProcessBuilder("ssh", "-V");
		try {
			b.redirectErrorStream(true);
			Process p = b.start();
			try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				return r.readLine().split("\\s+")[1].split(",")[0];
			} finally {
				p.waitFor();
			}
		} catch (InterruptedException ie) {
			return super.getVersion();
		} catch (IOException ioe) {
			return super.getVersion();
		}
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(DEFAULT_CAPS);
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		if (protocolVersion == SshConfiguration.SSH1_ONLY)
			return Arrays.asList("des", "3des", "blowfish", "rc4", "none");
		else {
			return readLines("ssh", "-Q", "cipher");
		}
	}

	public List<String> getSupportedCompression() {
		return Arrays.asList("zlib@openssh.com", "zlib", "none");
	}

	public List<String> getSupportedMAC() {
		return readLines("ssh", "-Q", "mac");
	}

	public List<String> getSupportedKeyExchange() {
		return readLines("ssh", "-Q", "kex");
	}

	public List<String> getSupportedPublicKey() {
		return readLines("ssh", "-Q", "key");
	}

	public void seed(long seed) {
	}

	private List<String> readLines(String... cmd) {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		List<String> ciphers = new ArrayList<>();
		try {
			Process p = pb.start();
			try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				String line = null;
				while ((line = r.readLine()) != null)
					ciphers.add(line);
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Cannot execute OpenSSH to query ciphers.", ioe);
		}
		return ciphers;
	}
}
