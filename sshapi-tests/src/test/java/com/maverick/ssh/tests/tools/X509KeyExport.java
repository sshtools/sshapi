package com.maverick.ssh.tests.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.maverick.ssh.tests.client.tests.X509AuthenticationIntegrationTest;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshKeyPair;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;

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
		SshConfiguration cfg = new SshConfiguration().addRequiredCapability(Capability.X509_PUBLIC_KEY);
		SshIdentityManager idm = DefaultProviderFactory.getInstance().getProvider(cfg).createIdentityManager(cfg);
		SshKeyPair keypair = idm.importX509(getClass().getResourceAsStream("/" + name + "/keystore"),
				X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray(), "mykey",
				X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray());
		File toFile = new File(new File(new File("resources"), name), "authorized_keys");
		SshPublicKeyFile file = idm.create(keypair.getPublicKey(), null, "test@localhost", SshPrivateKeyFile.VENDOR_OPENSSH);
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
