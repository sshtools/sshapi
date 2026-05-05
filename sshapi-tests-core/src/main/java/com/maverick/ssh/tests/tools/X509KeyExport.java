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
package com.maverick.ssh.tests.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
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
	private static final char[] PASSPHRASE = "changeit".toCharArray();
	private String name;

	public X509KeyExport() throws IOException {
		readOptions();
	}

	public void run() throws Exception {
		SshConfiguration cfg = new SshConfiguration();
		cfg.addRequiredCapability(Capability.IDENTITY_MANAGEMENT);
		cfg.addRequiredCapability(Capability.X509_PUBLIC_KEY);
		SshProvider prov = DefaultProviderFactory.getInstance().getProvider(cfg);
		SshIdentityManager idm = prov.createIdentityManager(cfg);
		SshPrivateKeyFile kf = idm.createPrivateKeyFromStream(getClass().getResourceAsStream("/" + name + "/keystore"), PASSPHRASE);
		if(kf.isEncrypted())
			kf.decrypt(PASSPHRASE);
		SshKeyPair kp = kf.toKeyPair();
		SshPublicKeyFile pkf = idm.create(kp.getPublicKey(), null, null, SshPublicKeyFile.OPENSSH_FORMAT);
		File toFile = new File(new File(new File(new File(new File("src"), "test"), "resources"), name), "authorized_keys");
		try (FileOutputStream fw = new FileOutputStream(toFile)) {
			fw.write(pkf.getFormattedKey());
		}
		System.out.println("Written public key to " + toFile);
		
//		KeyStore keystore = KeyStore.getInstance("PKCS12");
//		char[] keystorePassphrase = X509AuthenticationIntegrationTest.PASSPHRASE.toCharArray();
//		keystore.load(getClass().getResourceAsStream("/" + name + "/keystore"), keystorePassphrase);
//		X509Certificate x509 = (X509Certificate) keystore.getCertificate("mykey");
//		SshX509RsaSha1PublicKey pubkey = new SshX509RsaSha1PublicKey(x509);
//		File toFile = new File(new File(new File("resources"), name), "authorized_keys");
//		com.sshtools.publickey.SshPublicKeyFile file = com.sshtools.publickey.SshPublicKeyFileFactory.create(pubkey,
//				"test@localhost", SshPublicKeyFileFactory.OPENSSH_FORMAT);
//		try (FileOutputStream fw = new FileOutputStream(toFile)) {
//			fw.write(file.getFormattedKey());
//		}
//		System.out.println("Written public key to " + toFile);
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
