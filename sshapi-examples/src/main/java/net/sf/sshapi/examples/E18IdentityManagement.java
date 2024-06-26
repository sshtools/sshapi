package net.sf.sshapi.examples;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.StringTokenizer;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey.Algorithm;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshKeyPair;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates listing and changing known hosts.
 * <p>
 * Note, not all implementations identity management
 * 
 */
public class E18IdentityManagement {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.addRequiredCapability(Capability.IDENTITY_MANAGEMENT);

		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		System.out.println("Got provider " + provider.getClass());
		SshIdentityManager mgr = provider.createIdentityManager(config);
		//
		System.out.println("generate <type> <bits> - generate key. type may be one of " + mgr.getSupportedKeyTypes()
			+ ", size may be one of " + mgr.getSupportedKeyLengths() + ", format may be one of "
			+ mgr.getSupportedPublicKeyFileFormats());
		System.out
			.println("write <public-format> <private-format> <comment> - write out key. public format may be one of "
				+ mgr.getSupportedPublicKeyFileFormats() + ", private format may be one of "
				+ mgr.getSupportedPrivateKeyFileFormats());
		System.out.println("exit - quit this utility");
		String cmd = "";
		SshKeyPair keyPair = null;
		while (true) {
			cmd = Util.prompt("Command: ");
			if (cmd.startsWith("generate ")) {
				keyPair = generate(mgr, cmd);
			} else if (cmd.startsWith("write")) {
				write(mgr, cmd, keyPair);
			} else if (cmd.equals("load")) {
				break;
			} else if (cmd.equals("exit")) {
				break;
			} else {
				System.out.println("Invalid command");
			}
		}
	}

	protected static void load(SshIdentityManager mgr, String cmd, SshKeyPair keyPair) throws SshException, IOException {
		StringTokenizer t = new StringTokenizer(cmd);
		t.nextToken();
		String file = t.nextToken();
		String passphrase = t.hasMoreTokens() ? t.nextToken() : null;
		try(FileInputStream in = new FileInputStream(file)) {
			SshPrivateKeyFile kf = passphrase == null ? mgr.createPrivateKeyFromStream(in) : mgr.createPrivateKeyFromStream(in, passphrase.toCharArray());
			keyPair = kf.toKeyPair();
		}
	}

	protected static void write(SshIdentityManager mgr, String cmd, SshKeyPair keyPair) throws SshException, IOException {
		if (keyPair == null) {
			System.err.println("No key pair in memory. Use generate or load to get one.");
		} else {
			StringTokenizer t = new StringTokenizer(cmd);
			t.nextToken();
			int publicFormat = Integer.parseInt(t.nextToken());
			int privateFormat = Integer.parseInt(t.nextToken());
			String passphrase = t.nextToken();
			char[] pass = Util.nullOrTrimmedBlank(passphrase) ? null : passphrase.toCharArray();
			String comment = t.nextToken();
			if (Util.nullOrTrimmedBlank(comment)) {
				comment = "E18IdentityManagment example, part of SSHAPI";
			}
			SshPublicKeyFile pubKeyFile = mgr.create(keyPair.getPublicKey(), null, comment, publicFormat);
			SshPrivateKeyFile privKeyFile = mgr.create(keyPair, privateFormat, pass, comment);
			String formatted = new String(pubKeyFile.getFormattedKey());
			System.out.println("Public Key:" + formatted);
			System.out.println("Private Key:" + new String(privKeyFile.getFormattedKey()));
		}
	}

	protected static SshKeyPair generate(SshIdentityManager mgr, String cmd) throws SshException {
		SshKeyPair keyPair;
		StringTokenizer t = new StringTokenizer(cmd);
		t.nextToken();
		Algorithm algo = Algorithm.fromAlgoName(t.nextToken());
		int bits = Integer.parseInt(t.nextToken());
		keyPair = mgr.generateKeyPair(algo, bits);
		if (keyPair == null) {
			System.out.println("No key pair generated");
		} else {
			SshPublicKey publicKey = keyPair.getPublicKey();
			System.out.println("Generated " + publicKey.getFingerprint() + " using " + publicKey.getAlgorithm() + " with "
				+ publicKey.getBitLength());
		}
		return keyPair;
	}
}
