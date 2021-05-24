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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;

public class PublicKeyAuthenticationIntegrationTest extends AbstractClientConnecting {
	final static String PASSPHRASE = "changeit";

	@Test(expected = SshException.class)
	public void testInvalid() throws Exception {
		timeout(() -> {
			assertFalse("Authentication must be failed.", ssh.authenticate(createKey("invalid", "id_rsa", null)));
			return null;
		}, 10000);
	}

	@Test
	public void testRsaValid() throws Exception {
		assumeTrue("Server must support RSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_RSA));
		assumeTrue("Must support RSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHRSA));
		timeout(() -> {
			ssh.authenticate(createKey("rsa-valid", "id_rsa", null));
			return null;
		}, 10000);
	}

	@Test
	public void testRsaProtectedKey() throws Exception {
		assumeTrue("Server must support RSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_RSA));
		assumeTrue("Must support RSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHRSA));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("rsa-with-passphrase", "id_rsa", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testRsaProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support RSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_RSA));
		assumeTrue("Must support RSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHRSA));
		timeout(() -> {
			assertFalse("Authentication must be failed.",
					ssh.authenticate(createKey("rsa-with-passphrase", "id_rsa", PASSPHRASE + "XXXXXXXx")));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testDsaValid() throws Exception {
		assumeTrue("Server must support DSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_DSS));
		assumeTrue("Must support DSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHDSA));
		timeout(() -> {
			assertTrue("Authentication must be complete.", ssh.authenticate(createKey("dsa-valid", "id_dsa", null)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 1000000);
	}

	@Test
	public void testDsaProtectedKey() throws Exception {
		assumeTrue("Server must support DSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_DSS));
		assumeTrue("Must support DSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHDSA));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("dsa-with-passphrase", "id_dsa", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testDsaProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support DSA keys.", config.getKeyAlgorithms().contains(Algorithm.SSH_DSS));
		assumeTrue("Must support DSA keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_SSHDSA));
		timeout(() -> {
			boolean res = ssh.authenticate(createKey("dsa-with-passphrase", "id_dsa", PASSPHRASE + "XXXXXXXx"));
			assertFalse("Authentication must be failed.",
					res);
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
	
	@Test
	public void testEd25519Valid() throws Exception {
		assumeTrue("Server must support ED25519 keys.", config.getKeyAlgorithms().contains(Algorithm.ED25519));
		assumeTrue("Must support ED25519 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ED25519));
		timeout(() -> {
			ssh.authenticate(createKey("ed25519-valid", "id_ed25519", null));
			return null;
		}, 10000);
	}

	@Test
	public void testEd25519ProtectedKey() throws Exception {
		assumeTrue("Server must support ED25519 keys.", config.getKeyAlgorithms().contains(Algorithm.ED25519));
		assumeTrue("Must support ED25519 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ED25519));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("ed25519-with-passphrase", "id_ed25519", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEd25519ProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support ED25519 keys.", config.getKeyAlgorithms().contains(Algorithm.ED25519));
		assumeTrue("Must support ED25519 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ED25519));
		timeout(() -> {
			assertFalse("Authentication must be failed.",
					ssh.authenticate(createKey("ed25519-with-passphrase", "id_ed25519", PASSPHRASE + "XXXXXXXx")));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa256Valid() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 256 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_256));
		timeout(() -> {
			ssh.authenticate(createKey("ecdsa256-valid", "id_ecdsa", null));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa256ProtectedKey() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 256 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_256));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("ecdsa256-with-passphrase", "id_ecdsa", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa256ProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 256 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_256));
		timeout(() -> {
			assertFalse("Authentication must be failed.",
					ssh.authenticate(createKey("ecdsa256-with-passphrase", "id_ecdsa", PASSPHRASE + "XXXXXXXx")));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
	
	@Test
	public void testEcdsa384Valid() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 384 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_384));
		timeout(() -> {
			ssh.authenticate(createKey("ecdsa384-valid", "id_ecdsa", null));
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa384ProtectedKey() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 384 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_384));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("ecdsa384-with-passphrase", "id_ecdsa", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa384ProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 384 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_384));
		timeout(() -> {
			assertFalse("Authentication must be failed.",
					ssh.authenticate(createKey("ecdsa384-with-passphrase", "id_ecdsa", PASSPHRASE + "XXXXXXXx")));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
	
	@Test
	public void testEcdsa521Valid() throws Exception {
		assumeTrue("Must support ECDSA 521 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_521));
		timeout(() -> {
			ssh.authenticate(createKey("ecdsa521-valid", "id_ecdsa", null));
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa521ProtectedKey() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 521 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_521));
		timeout(() -> {
			assertTrue("Authentication must be complete.",
					ssh.authenticate(createKey("ecdsa521-with-passphrase", "id_ecdsa", PASSPHRASE)));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}

	@Test
	public void testEcdsa521ProtectedKeyWithBadPassword() throws Exception {
		assumeTrue("Server must support ECDSA keys.", config.getKeyAlgorithms().contains(Algorithm.ECDSA));
		assumeTrue("Must support ECDSA 521 keys.", ssh.getProvider().getSupportedPublicKey().contains(SshConfiguration.PUBLIC_KEY_ECDSA_521));
		timeout(() -> {
			assertFalse("Authentication must be failed.",
					ssh.authenticate(createKey("ecdsa521-with-passphrase", "id_ecdsa", PASSPHRASE + "XXXXXXXx")));
			assertTrue("Must be connected", ssh.isConnected());
			return null;
		}, 10000);
	}
	
}
