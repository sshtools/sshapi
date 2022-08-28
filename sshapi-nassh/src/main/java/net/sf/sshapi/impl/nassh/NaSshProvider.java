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
package net.sf.sshapi.impl.nassh;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.snf4j.core.SelectorLoop;

import net.sf.sshapi.AbstractProvider;
import net.sf.sshapi.Capability;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.util.Util;

/**
 * Native Provider implementation
 */
public class NaSshProvider extends AbstractProvider {
	/**
	 * Single X11 connection
	 */
	private SecureRandom rng;
	private List<String> kexs = new ArrayList<>();
	private List<String> macs = new ArrayList<>();
	private List<String> compressions = new ArrayList<>();
	private List<String> ciphers = new ArrayList<>();
	private List<String> hostKeyAlgos = new ArrayList<>();
	private SelectorLoop loop;
	private Object lock = new Object();

	/**
	 * Constructor.
	 */
	public NaSshProvider() {
		super("NaSsh", "https://github.com/sshtools/sshapi/sshapi-nassh");
		rng = new SecureRandom();
	}

	@Override
	public String getVersion() {
		return Util.getManifestVersion(NaSshProvider.class, "Build-Version");
	}

	protected SshClient doCreateClient(SshConfiguration configuration) {
		synchronized(lock) {
			if(loop == null) {
				try {
					loop = new SelectorLoop();
					loop.start();
				}
				catch(IOException ioe) {
					throw new IllegalStateException("Could not start loop.", ioe);
				}
			}
			SshClient client = new NaSshClient(configuration);
			client.init(this);
			return client;
		}
	}

	public void doSupportsConfiguration(SshConfiguration configuration) {
	}

	public List<Capability> getCapabilities() {
		return Arrays.asList(new Capability[] { });
	}

	public List<String> getSupportedCiphers(int protocolVersion) {
		if (protocolVersion == SshConfiguration.SSH1_ONLY) {
			throw new UnsupportedOperationException("Only SSH2 is supported by NaSSH");
		}
		return ciphers;
	}

	public List<String> getSupportedCompression() {
		return compressions;
	}

	public List<String> getSupportedMAC() {
		return macs;
	}

	public List<String> getSupportedKeyExchange() {
		return kexs;
	}

	public List<String> getSupportedPublicKey() {
		return hostKeyAlgos;
	}

	public void seed(long seed) {
		rng.setSeed(seed);
	}
	
	SelectorLoop getLoop() {
		return loop;
	}
}
