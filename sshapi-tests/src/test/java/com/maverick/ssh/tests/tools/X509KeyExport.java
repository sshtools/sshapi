package com.maverick.ssh.tests.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import com.maverick.ssh.components.jce.SshX509RsaSha1PublicKey;
import com.maverick.ssh.tests.client.tests.X509AuthenticationIntegrationTest;
import com.sshtools.common.publickey.SshPublicKeyFileFactory;

/**
 * This tool reads a PKCS12 keystore from resources and generates an OpenSSH
 * compatible public key to use to test authentication. This class must be run
 * in the source tree, the file generated will be located in the same resources
 * directory as the keystore named "authorized_keys". This should be APPENDED to
 * the end of authorized_keys on the test OpenSSH server.
 */
public class X509KeyExport {
	private String name;

	public X509KeyExport() throws IOException {
		readOptions();
	}

	public void run() throws Exception {
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		char[] keystorePassphrase = X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray();
		keystore.load(getClass().getResourceAsStream("/" + name + "/keystore"), keystorePassphrase);
		X509Certificate x509 = (X509Certificate) keystore.getCertificate("mykey");
		SshX509RsaSha1PublicKey pubkey = new SshX509RsaSha1PublicKey(x509);
		File toFile = new File(new File(new File("resources"), name), "authorized_keys");
		com.sshtools.publickey.SshPublicKeyFile file = com.sshtools.publickey.SshPublicKeyFileFactory.create(pubkey,
				"test@localhost", SshPublicKeyFileFactory.OPENSSH_FORMAT);
		try (FileOutputStream fw = new FileOutputStream(toFile)) {
			fw.write(file.getFormattedKey());
		}
		System.out.println("Written public key to " + toFile);
	}

	private void readOptions() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Test Group Name:");
		name = br.readLine();
	}

	public static void main(String[] args) throws Exception {
		new X509KeyExport().run();
	}
}
