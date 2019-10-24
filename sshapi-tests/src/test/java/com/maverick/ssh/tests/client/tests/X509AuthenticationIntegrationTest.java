package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertTrue;

import java.security.KeyStore;

import org.junit.Assume;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnecting;

import net.sf.sshapi.Capability;
import net.sf.sshapi.auth.SshPublicKeyAuthenticator;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshKeyPair;

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
		Assume.assumeTrue("Must support X509", ssh.getProvider().getCapabilities().contains(Capability.X509_PUBLIC_KEY));
		SshPublicKeyAuthenticator pk = createKey("x509-valid", PASSPHRASE);
		boolean result = ssh.authenticate(pk);
		assertTrue("Authentication must be complete.", result);
		assertTrue("Must be connected", ssh.isConnected());
	}

	private SshPublicKeyAuthenticator createKey(String key, String passphrase) throws Exception {
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		SshIdentityManager idm = ssh.getProvider().createIdentityManager(ssh.getConfiguration());
		SshKeyPair keypair = idm.importX509(getClass().getResourceAsStream("/" + key + "/keystore"),
				X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray(), "mykey",
				X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray());
		// TODO
		throw new UnsupportedOperationException();
//		return new DefaultPublicKeyAuthenticator(keypair);
	}
}
