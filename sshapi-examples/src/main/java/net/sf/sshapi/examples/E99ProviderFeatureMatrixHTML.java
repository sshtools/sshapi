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
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.sf.sshapi.Capability;
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;

/**
 * This example queries all of the providers to draw a feature matric table
 */
public class E99ProviderFeatureMatrixHTML {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		JFrame f = new JFrame("SSHAPI Capabilities");
		JTabbedPane tabs = new JTabbedPane();
		f.setLayout(new BorderLayout());
		f.add(tabs, BorderLayout.CENTER);
		tabs.addTab("HTML", new JScrollPane(new JEditorPane("text/html", getHTML())));
		tabs.addTab("Source", new JScrollPane(new JTextArea(getHTML())));
		f.pack();
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				System.exit(0);
			}
		});
	}

	static String getHTML() throws Exception {
		// List all of the providers and allow the user to select one
		SshProvider[] providers = DefaultProviderFactory.getAllProviders();
		Capability[] caps = DefaultProviderFactory.getAllCapabilties();
		String[] ciphers = DefaultProviderFactory.getAllCiphers();
		String[] key = DefaultProviderFactory.getAllKEX();
		String[] mac = DefaultProviderFactory.getAllMAC();
		String[] comp = DefaultProviderFactory.getAllCompression();
		String[] pk = DefaultProviderFactory.getAllPublicKey();
		String[] fp = DefaultProviderFactory.getAllFingerprintHashingAlgorithms();
		String[][] strArrs = new String[][] { ciphers, key, mac, comp, pk, fp };

		StringBuilder bui = new StringBuilder();
		bui.append("<html>\n");
		bui.append("<head>\n");
		bui.append("<style type=\"text/css\">\n");
		bui.append(".section { font-size: 32pt; font-weight: bold; }\n");
		bui.append("</style>\n");
		bui.append("</head>\n");
		bui.append("<body>\n");
		bui.append("  <table border=\"1\">\n");
		bui.append("    <thead>\n");
		bui.append("      <tr>\n");

		// Titles
		String[] titles = new String[providers.length + 1];
		for (int i = 0; i < 1 + providers.length; i++) {
			if (i == 0) {
				titles[i] = "Capability";
			} else {
				titles[i] = providers[i - 1].getName() + "<br/>" + providers[i - 1].getVersion() + "<br/>" + providers[i - 1].getVendor();
			}
			bui.append("        <td>");
			bui.append(titles[i]);
			bui.append("</td>\n");
		}

		bui.append("      </tr>\n");
		bui.append("   </thead>\n");
		bui.append("   <tbody>\n");
		writeSection(providers, bui, "Features");

		// Caps
		for (int i = 0; i < caps.length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(caps[i].getName());
			bui.append("</td>\n");
			for (int j = 0; j < providers.length; j++) {
				bui.append("        <td>");
				if (providers[j].getCapabilities().contains(caps[i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
			bui.append("      </tr>\n");
		}

		// Ciphers
		writeSection(providers, bui, "Ciphers");
		for (int i = 0; i < strArrs[0].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[0][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getSupportedCiphers(SshConfiguration.SSH1_OR_SSH2).contains(strArrs[0][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
			bui.append("      </tr>\n");
		}

		// KEX
		writeSection(providers, bui, "Key Exchange");
		for (int i = 0; i < strArrs[1].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[1][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getSupportedKeyExchange().contains(strArrs[1][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
			bui.append("      </tr>\n");
		}

		// MAC
		writeSection(providers, bui, "MAC");
		for (int i = 0; i < strArrs[2].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[2][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getSupportedMAC().contains(strArrs[2][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
			bui.append("      </tr>\n");
		}

		// Compression
		writeSection(providers, bui, "Compression");
		for (int i = 0; i < strArrs[3].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[3][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getSupportedCompression().contains(strArrs[3][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
			bui.append("      </tr>\n");
		}

		// Public Key
		writeSection(providers, bui, "Public Key");
		for (int i = 0; i < strArrs[4].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[4][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getSupportedPublicKey().contains(strArrs[4][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
		}

		// Fingerprint algorithms
		writeSection(providers, bui, "Fingerprint Algos.");
		for (int i = 0; i < strArrs[5].length; i++) {
			bui.append("      <tr>\n");
			bui.append("        <td>");
			bui.append(strArrs[5][i]);
			bui.append("</td>\n");
			for (int k = 0; k < providers.length; k++) {
				bui.append("        <td>");
				if (providers[k].getFingerprintHashingAlgorithms().contains(strArrs[5][i])) {
					bui.append("X");
				}
				bui.append("</td>\n");
			}
		}

		bui.append("   </tbody>\n");
		bui.append(" </table>\n");
		bui.append("</body>\n");
		bui.append("</html>\n");

		return bui.toString();
	}

	private static void writeSection(SshProvider[] providers, StringBuilder bui, String name) {
		bui.append("      <tr class=\"section\">\n");
		bui.append("        <td colspan=\"" + ( providers.length + 1 ) + "\">");
		bui.append(name);
		bui.append("</td>\n");
		bui.append("      </tr>\n");
	}
}
