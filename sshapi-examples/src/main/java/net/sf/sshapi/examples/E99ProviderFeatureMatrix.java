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
import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;

/**
 * This example queries all of the providers to draw a feature matrix table in
 * the 'APT' format (http://maven.apache.org/doxia/references/apt-format.html).
 * This is used actually in the API documentation as well.
 */
public class E99ProviderFeatureMatrix {
	/**
	 * Entry point.
	 * 
	 * @param arg
	 *            command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// List all of the providers and allow the user to select one
		SshProvider[] providers = DefaultProviderFactory.getAllProviders();
		Capability[] caps = DefaultProviderFactory.getAllCapabilties();
		String[] ciphers = DefaultProviderFactory.getAllCiphers();
		String[] key = DefaultProviderFactory.getAllKEX();
		String[] mac = DefaultProviderFactory.getAllMAC();
		String[] comp = DefaultProviderFactory.getAllCompression();
		String[] pk = DefaultProviderFactory.getAllPublicKey();
		String[] fp = DefaultProviderFactory.getAllFingerprintHashingAlgorithms();

		// Calculate max width of cell
		int width = 0;
		for (int i = 0; i < providers.length; i++) {
			width = Math.max(width, providers[i].getClass().getSimpleName()
					.length());
		}
		for (int i = 0; i < caps.length; i++) {
			width = Math.max(width, caps[i].getName().length());
		}
		String[][] strArrs = new String[][] { ciphers, key, mac, comp, pk, fp };
		for (int j = 0; j < strArrs.length; j++) {
			for (int i = 0; i < strArrs[j].length; i++) {
				width = Math.max(width, strArrs[j][i].length());
			}
		}

		for (int i = 0; i < ciphers.length; i++) {
			width = Math.max(width, ciphers[i].length());
		}

		// Titles
		String[] titles = new String[providers.length + 1];
		StringBuffer sep = new StringBuffer("*");
		for (int i = 0; i < 1 + providers.length; i++) {
			if (i == 0) {
				titles[i] = "Capability";
			} else {
				titles[i] = providers[i - 1].getName();
			}
			if (i > 0) {
				sep.append("*");
			}
			for (int j = 0; j < width; j++) {
				sep.append("-");
			}
		}
		sep.append("*");
		
		System.out.println("Capabilities\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());

		// Caps
		for (int i = 0; i < caps.length; i++) {
			List<String> l = new ArrayList<>();
			l.add(caps[i].getName());
			for (int j = 0; j < providers.length; j++) {
				if (providers[j].getCapabilities().contains(caps[i])) {
					l.add("X");
				} else {
					l.add(" ");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// Ciphers
		System.out.println("\nCiphers\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[0].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[0][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getSupportedCiphers(
							SshConfiguration.SSH1_OR_SSH2).contains(
							strArrs[0][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// KEX
		System.out.println("\nKEX\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[1].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[1][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getSupportedKeyExchange().contains(
							strArrs[1][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// MAC
		System.out.println("\nMAC\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[2].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[2][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getSupportedMAC().contains(strArrs[2][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// Compression
		System.out.println("\nCompression\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[3].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[3][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getSupportedCompression().contains(
							strArrs[3][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// Publick Key
		System.out.println("\nPublic Key\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[4].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[4][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getSupportedPublicKey().contains(
							strArrs[4][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}

		// Fingerprint Algorithms
		System.out.println("\nFingerprint Algos.\n");
		System.out.println(sep);
		print(true, titles, width, sep.toString());
		for (int i = 0; i < strArrs[5].length; i++) {
			List<String> l = new ArrayList<>();
			l.add(strArrs[5][i]);
			for (int k = 0; k < providers.length; k++) {
				try {
					if (providers[k].getFingerprintHashingAlgorithms().contains(
							strArrs[5][i])) {
						l.add("X");
					} else {
						l.add(" ");
					}
				} catch (Exception e) {
					l.add("ERR");
				}
			}
			print(false, (String[]) l.toArray(new String[0]), width,
					sep.toString());
		}
	}

	static void print(boolean header, String[] args, int width, String sep) {
		StringBuffer buf = new StringBuffer();
		buf.append("|");
//		if (header) {
//			buf.append("|");
//		}
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				buf.append("|");
//				if (header) {
//					buf.append("|");
//				}
			}
			int before = (width - args[i].length()) / 2;
			int after = width - before - args[i].length();
			for (int j = 0; j < before; j++) {
				buf.append(" ");
			}
			buf.append(args[i]);
			for (int j = 0; j < after; j++) {
				buf.append(" ");
			}
		}
		buf.append("|");
		System.out.println(buf);
		System.out.println(sep);
	}
}
