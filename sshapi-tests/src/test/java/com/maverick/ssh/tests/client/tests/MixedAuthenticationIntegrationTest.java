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
		assertCapabilities(ServerCapability.CAN_DO_MULTIFACTOR_AUTH);
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
