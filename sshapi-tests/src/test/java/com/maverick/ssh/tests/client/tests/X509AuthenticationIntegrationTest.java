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

import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.Capability;
import net.sf.sshapi.auth.SshX509PublicKeyAuthenticator;
import net.sf.sshapi.util.DefaultX509PublicKeyAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

/**
 * <p>
 * Tests for X509 certificate authentication. To generate test keystores :-
 * <blockquote>
 * 
 * <pre>
 * 
 * keytool -genkeypair -keystore keystore -storepass changeit -storetype PKCS12 -keyalg rsa
 * </pre>
 * 
 * </blockquote>
 * </p>
 */
public class X509AuthenticationIntegrationTest extends AbstractClientConnecting {
	public final static String PASSPHRASE = "changeit";

	/**
	 * Ensures a valid X509 certificate authenticates.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testX509Valid() throws Exception {
		assertServerCapabilities(ServerCapability.X509);
		Assume.assumeTrue("Must support X509", ssh.getProvider().getCapabilities().contains(Capability.X509_PUBLIC_KEY));
		SshX509PublicKeyAuthenticator pk = new DefaultX509PublicKeyAuthenticator("mykey", new SimplePasswordAuthenticator(X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray()),
				getClass().getResourceAsStream("/x509-valid/keystore"));
		boolean result = ssh.authenticate(pk);
		assertTrue("Authentication must be complete.", result);
		assertTrue("Must be connected", ssh.isConnected());
	}

}
