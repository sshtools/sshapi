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
package net.sf.sshapi.impl.libssh;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.CallbackProxy;

import ssh.SshLibrary;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.hostkeys.SshHostKeyManager;

public class LibsshSshProvider extends AbstractProvider {
	
	static class JavaThreading implements CallbackProxy {

		public Object callback(Object[] args) {
			// TODO Auto-generated method stub
			return null;
		}

		public Class[] getParameterTypes() {
			// TODO Auto-generated method stub
			return null;
		}

		public Class getReturnType() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	static {
		SshLibrary.INSTANCE.ssh_init();
	}

	public LibsshSshProvider() {
		super("libssh");
	}

	protected SshClient doCreateClient(SshConfiguration configuration) {
		SshClient client = new LibsshClient(configuration);
		client.init(this);
		return client;
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
		try {
			SshLibrary.INSTANCE.hashCode();
		} catch (Exception e) {
			throw new UnsupportedOperationException("libssh is not installed");
		}
	}

	public List getCapabilities() {
		return Arrays.asList(new Capability[] { Capability.PASSWORD_AUTHENTICATION, Capability.PER_CONNECTION_CONFIGURATION,
			Capability.SSH2, Capability.SSH1, Capability.SCP, Capability.SFTP, Capability.FILE_TRANSFER_EVENTS });
	}

	public List getSupportedCiphers(int protocolVersion) {
		return Arrays.asList(new String[] { "aes128-cbc", "aes192-cbc", "aes256-cbc", "blowfish-cbc", "3des", "aes128-ctr" });
	}

	public SshHostKeyManager createHostKeyManager(SshConfiguration configuration) throws SshException {
		throw new UnsupportedOperationException();
	}

	public List getSupportedCompression() {
		return Arrays.asList(new String[] { "zlib", "zlib@openssh.com" });
	}

	public List getSupportedMAC() {
		return Arrays.asList(new String[] { "hmac-md5", "hmac-sha1", "hmac-sha1-96", "hmac-md5-96" });
	}

	public List getSupportedKeyExchange() {
		return Arrays.asList(new String[] { "diffie-hellman-group1-sha1" });
	}

	public List getSupportedPublicKey() {
		return Arrays.asList(new String[] { SshConfiguration.PUBLIC_KEY_SSHRSA, SshConfiguration.PUBLIC_KEY_SSHDSA });
	}

	public void seed(long seed) {
		// TODO seed libssh?
	}
}
