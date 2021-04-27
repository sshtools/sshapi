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
package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.AbstractSshTest;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.hostkeys.SshHostKey;
import net.sf.sshapi.hostkeys.SshHostKeyValidator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;
import net.sf.sshapi.util.Util;

public class HostKeyVerificationIntegrationTest extends AbstractSshTest {
	class FingerprintHostKeyVerification implements SshHostKeyValidator {
		private String[] fingerprints;

		FingerprintHostKeyVerification(String... fingerprints) {
			this.fingerprints = fingerprints;
		}

		@Override
		public int verifyHost(SshHostKey hostKey) throws net.sf.sshapi.SshException {
			String actual = hostKey.getFingerprint();
			for (String fp : fingerprints) {
				System.out.println("Comparing " + fp + " against " + actual);
				System.out.println(
						" for " + hostKey.getKey().length + " bytes: " + Util.formatAsHexString(hostKey.getKey(), ":"));
				if (actual.equals(fp))
					return STATUS_HOST_KEY_VALID;
			}
			return STATUS_HOST_KEY_UNKNOWN;
		}
	}

	@Test
	public void testCorrectFingerprint() throws Exception {
		timeout(() -> {
			String[] expected = config.getFingerprints();
			/*
			 * Do one connection to get the current fingerprint. Use this
			 */
			if (expected == null || expected.length == 0) {
				SshConfiguration icon = new SshConfiguration();
				List<String> fp = new ArrayList<>();
				icon.setHostKeyValidator((v) -> {
					fp.add(v.getFingerprint());
					return SshHostKeyValidator.STATUS_HOST_KEY_VALID;
				});
				try (SshClient c = icon.open(config.getUsername(), config.getServer(), config.getPort(),
						new SimplePasswordAuthenticator(config.getPassword()))) {
				}
				expected = new String[] { fp.get(0) };
			}
			SshConfiguration con = new SshConfiguration();
			con.setHostKeyValidator(new FingerprintHostKeyVerification(expected));
			try (SshClient c = con.open(config.getUsername(), config.getServer(), config.getPort(),
					new SimplePasswordAuthenticator(config.getPassword()))) {
				Assume.assumeTrue("Must support host key verification",
						c.getProvider().getCapabilities().contains(Capability.HOST_KEY_VERIFICATION));
				assertTrue("Must be connected", c.isConnected());
				assertTrue("Must be authenticated", c.isAuthenticated());
			}
			return null;
		}, 10000);
	}

	@Test(expected = SshException.class)
	public void testBadFingerprint() throws Exception {
		timeout(() -> {
			SshConfiguration con = new SshConfiguration();
			con.setHostKeyValidator(new FingerprintHostKeyVerification("xxxx"));
			SshProvider prov = DefaultProviderFactory.getInstance().getProvider(con);
			Assume.assumeTrue("Must support host key verification",
					prov.getCapabilities().contains(Capability.HOST_KEY_VERIFICATION));
			SshClient ssh = prov.open(con, config.getUsername(), config.getServer(), config.getPort(),
					new SimplePasswordAuthenticator(config.getPassword()));
			ssh.close();
			return null;
		}, 10000);
	}

	@Test(expected = SshException.class)
	public void testTimeoutFingerprint() throws Exception {
		timeout(() -> {
			SshConfiguration con = new SshConfiguration();
			AtomicBoolean flag = new AtomicBoolean();
			con.setHostKeyValidator((key) -> {
				try {
					Thread.sleep(180000);
					flag.set(true);
				} catch (InterruptedException e) {
				}
				return SshHostKeyValidator.STATUS_HOST_KEY_UNKNOWN;
			});
			SshProvider prov = DefaultProviderFactory.getInstance().getProvider(con);
			Assume.assumeTrue("Must support data timeouts",prov.getCapabilities().contains(Capability.IO_TIMEOUTS));
			Assume.assumeTrue("Must support host key verification",
					prov.getCapabilities().contains(Capability.HOST_KEY_VERIFICATION));
			SshClient ssh = prov.createClient(con);
			ssh.setTimeout(30000);
			ssh.connect(config.getUsername(), config.getServer(), config.getPort(), new SimplePasswordAuthenticator(config.getPassword()));
			ssh.close();
			Assert.assertFalse("Mustn't have finished sleep while prompting.", flag.get());
			Assert.assertFalse("Mustn't have terminated because of test interrupt.", isTimedOut());
			return null;
		}, 300000);
	}
}
