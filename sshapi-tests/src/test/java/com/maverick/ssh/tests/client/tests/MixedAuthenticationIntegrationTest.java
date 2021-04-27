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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.maverick.ssh.tests.ServerCapability;
import com.maverick.ssh.tests.ServerService.AuthenticationMethod;
import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.auth.SshKeyboardInteractiveAuthenticator;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.util.SimplePasswordAuthenticator;

public class MixedAuthenticationIntegrationTest extends
		AbstractClientConnecting {
	
	@Before
	public void checkServerCaps() {
		assertServerCapabilities(ServerCapability.CAN_DO_MULTIFACTOR_AUTH);
	}

	@Test
	public void testDsaValidAndPasswordValid() throws Exception {
		configureForMethods(AuthenticationMethod.PASSWORD,
				AuthenticationMethod.PUBLICKEY);
		try {
			// Stage 1
			SshPublicKeyAuthenticator pk = createKey("dsa-valid", "id_dsa", null);
			boolean result = ssh.authenticate(pk);
			assertFalse("More Authentication should be required.", result);

			// Stage 2
			SimplePasswordAuthenticator pwd = new SimplePasswordAuthenticator(config.getPassword());
			result = ssh.authenticate(pwd);
			assertTrue("Authentication must be complete.",result);

			// Done
			assertTrue("Must be connected", ssh.isConnected());
		} finally {
			deconfigureForMethods();
		}
	}

	@Test
	public void testDsaValidAndKBIValid() throws Exception {
		configureForMethods(AuthenticationMethod.KEYBOARD_INTERACTIVE,
				AuthenticationMethod.PUBLICKEY);
		try {
			// Stage 1
			SshPublicKeyAuthenticator pk = createKey("dsa-valid", "id_dsa", null);
			boolean result = ssh.authenticate(pk);
			assertFalse("More Authentication should be required.", result);

			// Stage 2
			result = ssh.authenticate(createKBI());
			assertTrue("Authentication must be complete.", result);

			// Done
			assertTrue("Must be connected", ssh.isConnected());
		} finally {
			deconfigureForMethods();
		}
	}

	private SshKeyboardInteractiveAuthenticator createKBI() {
		SshKeyboardInteractiveAuthenticator kbi = new SshKeyboardInteractiveAuthenticator() {
			@Override
			public String[] challenge(String name, String instruction, String[] prompt, boolean[] echo) {
				String[] responses = new String[prompt.length];
				for (int i = 0; i < prompt.length; i++) {
					assertEquals(config.getChallenge(i),
							prompt[i]);
					responses[i] = config.getResponse(i);
				}
				return responses;
			}
		};
		return kbi;
	}

	@Test
	public void testDsaValidPasswordValidKBIValid() throws Exception {
		configureForMethods(AuthenticationMethod.PUBLICKEY,
				AuthenticationMethod.KEYBOARD_INTERACTIVE,
				AuthenticationMethod.PASSWORD);
		try {
			// Stage 1
			SshPublicKeyAuthenticator pk = createKey("dsa-valid", "id_dsa", null);
			boolean result = ssh.authenticate(pk);
			assertFalse("More Authentication should be required.", result);

			// Stage 2
			result = ssh.authenticate(createKBI());
			assertFalse("More Authentication should be required.", result);

			// Stage 3
			SimplePasswordAuthenticator pwd = new SimplePasswordAuthenticator(config.getPassword());
			result = ssh.authenticate(pwd);
			assertTrue("Authentication must be complete.", result);

			// Done
			assertTrue("Must be connected", ssh.isConnected());
			
		} finally {
			deconfigureForMethods();
		}
	}

	@Test
	public void testDsaInvalid() throws Exception {
		configureForMethods(AuthenticationMethod.PUBLICKEY,
				AuthenticationMethod.KEYBOARD_INTERACTIVE,
				AuthenticationMethod.PASSWORD);
		try {
			SshPublicKeyAuthenticator pk = createKey("dsa-invalid", "id_dsa",
					null);
			boolean result = ssh.authenticate(pk);
			assertFalse("Authentication must be failed.", result);
		} finally {
			deconfigureForMethods();
		}
	}
	

	@Test
	public void testDsaValidPasswordInvalid() throws Exception {
		configureForMethods(AuthenticationMethod.PUBLICKEY,
				AuthenticationMethod.PASSWORD);
		try {
			// Stage 1
			SshPublicKeyAuthenticator pk = createKey("dsa-valid", "id_dsa", null);
			boolean result = ssh.authenticate(pk);
			assertFalse("More Authentication should be required.", result);

			// Stage 2
			SimplePasswordAuthenticator pwd = new SimplePasswordAuthenticator((new String(config.getPassword()) + "XXXXXXXXx").toCharArray());
			result = ssh.authenticate(pwd);
			assertFalse("Authentication must be failed.",result);

			// Done
			assertTrue("Must be connected", ssh.isConnected());
		} finally {
			deconfigureForMethods();
		}
	}

}
