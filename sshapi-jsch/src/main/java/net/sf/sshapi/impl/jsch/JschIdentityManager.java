/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.impl.jsch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshPrivateKey;
import net.sf.sshapi.SshPublicKey;
import net.sf.sshapi.identity.SshIdentityManager;
import net.sf.sshapi.identity.SshKeyPair;
import net.sf.sshapi.identity.SshPrivateKeyFile;
import net.sf.sshapi.identity.SshPublicKeyFile;
import net.sf.sshapi.util.Util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

class JschIdentityManager implements SshIdentityManager {

	private JSch jsch;

	public JschIdentityManager(SshConfiguration configuration) {
		jsch = new JSch();
	}

	@Override
	public SshPrivateKeyFile createPrivateKeyFromStream(InputStream in) throws SshException {
		try {
			File file = toTemporaryFile(in, "");
			KeyPair kpair = KeyPair.load(jsch, file.getAbsolutePath());
			return new JschPrivateKeyFile(kpair);
		} catch (JSchException e) {
			throw new SshException(e);
		} catch (IOException ioe) {
			throw new SshException(SshException.IO_ERROR, ioe);
		}
	}

	@Override
	public SshPublicKeyFile createPublicKeyFromStream(InputStream in) throws SshException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Integer> getSupportedPublicKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPublicKeyFile.OPENSSH_FORMAT),
			new Integer(SshPublicKeyFile.SECSH_FORMAT) });
	}

	@Override
	public List<Integer> getSupportedPrivateKeyFileFormats() {
		return Arrays.asList(new Integer[] { new Integer(SshPublicKeyFile.OPENSSH_FORMAT),
			new Integer(SshPublicKeyFile.SECSH_FORMAT) });
	}

	@Override
	public List<Integer> getSupportedKeyLengths() {
		return Arrays.asList(new Integer[] { new Integer(2048), new Integer(1024), new Integer(768), new Integer(512) });
	}

	@Override
	public List<String> getSupportedKeyTypes() {
		return Arrays.asList(new String[] { "rsa", "dsa" });
	}

	@Override
	public SshPrivateKeyFile create(SshKeyPair pair, int format, char[] passphrase, String comment) throws SshException {
		if (!Util.nullOrTrimmedBlank(comment)) {
			SshConfiguration.getLogger().log(Level.WARN,
				"JSch doesn't support comments in private key files, ignoring comment '" + comment + "'");
		}
		if (format != SshPrivateKeyFile.VENDOR_OPENSSH) {
			throw new SshException(SshException.UNSUPPORTED_FEATURE, "Private key file format not supported.");
		}

		// JSch doesn't make this easy :(
		try {
			File tempFile = File.createTempFile("createpk", ".tmp");
			try {
				// Get the private key from the current key pair
				KeyPair kpair = ((JschPrivateKey) pair.getPrivateKey()).kpair;
				if (kpair.isEncrypted()) {
					throw new SshException(SshException.ENCRYPTED, "Attempt to access a private key that is currently encrypted.");
				}
				kpair.writePrivateKey(tempFile.getAbsolutePath());

				// Get a copy
				KeyPair kPairCopy = KeyPair.load(jsch, tempFile.getAbsolutePath());
				if (passphrase != null) {
					kPairCopy.setPassphrase(new String(passphrase));
					kPairCopy.writePrivateKey(tempFile.getAbsolutePath());
					kPairCopy = KeyPair.load(jsch, tempFile.getAbsolutePath());
					kPairCopy.decrypt(new String(passphrase));
				}

				JschPrivateKeyFile privateKeyFile = new JschPrivateKeyFile(kPairCopy);
				return privateKeyFile;
			} finally {
				tempFile.delete();
			}
		} catch (SshException sshe) {
			throw sshe;
		} catch (Exception e) {
			throw new SshException(e);
		}
	}

	@Override
	public SshKeyPair generateKeyPair(String keyType, int keyBits) throws SshException {
		try {
			int type = -1;
			if (keyType.equals(SshConfiguration.PUBLIC_KEY_SSHRSA)) {
				type = KeyPair.RSA;
			} else if (keyType.equals(SshConfiguration.PUBLIC_KEY_SSHDSA)) {
				type = KeyPair.DSA;
			} else if (keyType.equals(SshConfiguration.PUBLIC_KEY_ECDSA)) {
				type = KeyPair.ECDSA;
			} else {
				throw new SshException(SshException.UNSUPPORTED_FEATURE, "Algoritm " + keyType + " is not supported.");
			}
			KeyPair kpair = KeyPair.genKeyPair(jsch, type, keyBits);
			return new SshKeyPair(new JschPublicKey(kpair, keyBits), new JschPrivateKey(kpair));
		} catch (JSchException e) {
			throw new SshException(e);
		}
	}

	static File toTemporaryFile(InputStream in, String suffix) throws IOException {
		File f = File.createTempFile("jsch", suffix);
		FileOutputStream fos = new FileOutputStream(f);
		try {
			int b;
			while ((b = in.read()) != -1) {
				fos.write(b);
			}
		} finally {
			fos.close();
		}
		return f;
	}

	class JschPrivateKeyFile implements SshPrivateKeyFile {

		private KeyPair kpair;

		public JschPrivateKeyFile(KeyPair kpair) {
			this.kpair = kpair;
		}

		@Override
		public byte[] getFormattedKey() throws SshException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			kpair.writePrivateKey(baos);
			return baos.toByteArray();
		}

		@Override
		public void changePassphrase(char[] newPassphrase) throws SshException {
			if (kpair.isEncrypted()) {
				throw new SshException(SshException.PASSPHRASE_REQUIRED,
					"Key is currently encrypyted. Please decrypt before changing passphrase.");
			}
			kpair.setPassphrase(new String(newPassphrase));
		}

		@Override
		public boolean isEncrypted() throws SshException {
			return kpair.isEncrypted();
		}

		@Override
		public void decrypt(char[] passphrase) throws SshException {
			if (!kpair.decrypt(new String(passphrase))) {
				throw new SshException(SshException.INCORRECT_PASSPHRASE, "Incorrect passphrase, could not decrypt key.");
			}
		}

		@Override
		public boolean supportsPassphraseChange() {
			return true;
		}

		@Override
		public int getFormat() {
			try {
				Field amF = kpair.getClass().getDeclaredField("vendor");
				amF.setAccessible(true);
				int vendor = ((Integer) amF.get(kpair)).intValue();
				if (vendor == 0) {
					return VENDOR_OPENSSH;
				} else if (vendor == 1) {
					return VENDOR_FSECURE;
				}
			} catch (Exception e) {
			}
			return VENDOR_UNKNOWN;
		}

		@Override
		public SshKeyPair toKeyPair() throws SshException {
			if (isEncrypted()) {
				throw new SshException(SshException.PASSPHRASE_REQUIRED,
					"Key is currently encrypyted. Please decrypt before changing passphrase.");
			}
			System.out.println("KEYBITS: " + kpair.getPublicKeyBlob().length);
			return new SshKeyPair(new JschPublicKey(kpair, kpair.getPublicKeyBlob().length), new JschPrivateKey(kpair));
		}

	}

	class JschPrivateKey implements SshPrivateKey {

		private KeyPair kpair;

		public JschPrivateKey(KeyPair kpair) {
			this.kpair = kpair;
		}

		@Override
		public byte[] sign(byte[] data) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getAlgorithm() {
			switch(kpair.getKeyType()) {
			case KeyPair.DSA:
				return "dsa";
			case KeyPair.ECDSA:
				return "ecdsa";
			case KeyPair.RSA:
				return "rsa";
			case KeyPair.ERROR:
				return "error";
			default:
				return "unknown";
			}
		}

	}

	class JschPublicKey implements SshPublicKey {

		private int keyBits;
		private KeyPair keyPair;

		public JschPublicKey(KeyPair keyPair, int keyBits) {
			this.keyPair = keyPair;
			this.keyBits = keyBits;
		}

		@Override
		public String getAlgorithm() {
			if(keyPair.getKeyType() == KeyPair.DSA)
				return SshConfiguration.PUBLIC_KEY_SSHDSA;
			else if(keyPair.getKeyType() == KeyPair.RSA)
				return SshConfiguration.PUBLIC_KEY_SSHRSA;
			else if(keyPair.getKeyType() == KeyPair.ECDSA)
				return SshConfiguration.PUBLIC_KEY_ECDSA;
			else 
				throw new IllegalStateException("Unsupported public key algorithm.");
				
		}

		@Override
		public String getFingerprint() throws SshException {
			return keyPair.getFingerPrint();
		}

		@Override
		public byte[] getEncodedKey() throws SshException {
			return keyPair.getPublicKeyBlob();
		}

		@Override
		public int getBitLength() {
			return keyBits;
		}

	}

	@Override
	public SshPublicKeyFile create(final SshPublicKey key, final String options, final String comment, final int format) {
		final JschPublicKey jschKey = (JschPublicKey) key;
		if (format != SshPublicKeyFile.SECSH_FORMAT && format != SshPublicKeyFile.OPENSSH_FORMAT) {
			throw new UnsupportedOperationException("Unsupported public key file type.");
		}
		return new SshPublicKeyFile() {

			@Override
			public SshPublicKey getPublicKey() throws SshException {
				return key;
			}

			@Override
			public String getOptions() {
				return options;
			}

			@Override
			public byte[] getFormattedKey() throws IOException {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				switch (format) {
				case SshPublicKeyFile.SECSH_FORMAT:
					jschKey.keyPair.writeSECSHPublicKey(baos, comment);
					break;
				case SshPublicKeyFile.OPENSSH_FORMAT:
					jschKey.keyPair.writePublicKey(baos, comment);
					break;
				default:
					throw new UnsupportedOperationException("Public key file format not supported.");
				}
				return baos.toByteArray();
			}

			@Override
			public String getComment() {
				return comment;
			}

			@Override
			public int getFormat() {
				return format;
			}
		};
	}

	public SshPublicKeyFile parse(byte[] loadFile) {
		throw new UnsupportedOperationException();
	}

}
