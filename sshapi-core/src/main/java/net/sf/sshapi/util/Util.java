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
package net.sf.sshapi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.sftp.SftpFile;

/**
 * Various utilities
 */
public class Util {

	/**
	 * Permissions flag: Format mask constant can be used to mask off a file
	 * type from the mode.
	 */
	public static final int S_IFMT = 0xF000;

	/**
	 * Permissions flag: Bit to determine whether a file is executed as the
	 * owner
	 */
	public final static int S_ISUID = 0x800;

	/**
	 * Permissions flag: Bit to determine whether a file is executed as the
	 * group owner
	 */
	public final static int S_ISGID = 0x400;

	/**
	 * Convert a string array into a delimited string.
	 * 
	 * @param arr array of strings
	 * @param delimiter delimiter
	 * @return delimited string
	 */
	public static String toDelimited(String[] arr, char delimiter) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0) {
				buf.append(delimiter);
			}
			buf.append(arr[i]);
		}
		return buf.toString();
	}

	/**
	 * Prompt for a yes / no answer on the console
	 * 
	 * @param message message to display
	 * @return answer (<code>true</code> for yes)
	 */
	public static boolean promptYesNo(String message) {
		String answer = prompt(message + " - (Y)es or (N)o?");
		return answer != null && (answer.toLowerCase().equals("y") || answer.toLowerCase().equals("yes"));
	}

	/**
	 * Prompt for an answer on the console
	 * 
	 * @param message message to display
	 * @return answer
	 */
	public static String prompt(String message) {
		message = message.trim();
		System.out.print(message + (message.endsWith(":") ? " " : ": "));
		System.out.flush();
		try {
			return readLine();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Format a byte array as a hex string.
	 * 
	 * @param arr byte array
	 * @return hex string
	 */
	public static String formatAsHexString(byte[] arr) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			buf.append(toPaddedHexString(arr[i], 2));
		}
		return buf.toString();
	}

	private static String toPaddedHexString(byte val, int size) {
		String s = Integer.toHexString(val & 0xff);
		while (s.length() < size) {
			s = "0" + s;
		}
		return s;
	}

	private static String readLine() throws IOException {
		String answer = new BufferedReader(new InputStreamReader(System.in)).readLine();
		return answer;
	}
	
	/**
	 * Display a prompt asking for an SSH user and host (and optional port) to connect
	 * to. Useful to pass to various methods to open connections in SSHAPI, such as 
	 * {@link SshClient#connect(String, net.sf.sshapi.auth.SshAuthenticator...)}, or
	 * {@link Ssh#open(String, net.sf.sshapi.auth.SshAuthenticator...)}.
	 * 
	 * @return connection spec
	 */
	public static String promptConnectionSpec() {
		return prompt("Enter username@hostname", System.getProperty("user.name") + "@localhost");
	}

	/**
	 * Display a prompt on the console and wait for an answer.
	 * 
	 * @param message message to display to use
	 * @param defaultValue value to return if user just presses RETURN
	 * @return user input
	 */
	public static String prompt(String message, String defaultValue) {
		String fullMessage = message;
		fullMessage += " (RETURN for a default value of " + defaultValue + ")";
		String val = prompt(fullMessage);
		return val.equals("") ? defaultValue : val;
	}

	/**
	 * Get the file portion of a path, i.e the part after the last /. If the
	 * provided path ends with a /, this is stripped first.
	 * 
	 * @param path path
	 * @return file basename
	 */
	public static String basename(String path) {
		if (path.equals("/")) {
			return path;
		}
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		int idx = path.lastIndexOf("/");
		return idx == -1 ? path : path.substring(idx + 1);
	}

	/**
	 * Concatenate two paths, removing any additional leading/trailing slashes.
	 * 
	 * @param path first path portion
	 * @param filename path to append
	 * @return concatenated path
	 */
	public static String concatenatePaths(String path, String filename) {
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		return path + "/" + filename;
	}

	/**
	 * Parse a string of bytes encoded as hexadecimal values. Each byte is
	 * represented by two characters.
	 * 
	 * @param string hex string
	 * @return data
	 */
	public static byte[] parseHexString(String string) {
		if (string.length() % 2 == 1) {
			throw new IllegalArgumentException("Not a hex string");
		}
		byte[] arr = new byte[string.length() / 2];
		for (int i = 0; i < string.length(); i += 2) {
			arr[i / 2] = Integer.valueOf(string.substring(i, i + 2), 16).byteValue();
		}
		return arr;
	}

	/**
	 * Copy bytes read from an input stream to an output stream, until the end
	 * of the input stream is reached.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @throws IOException
	 */
	public static void joinStreams(InputStream in, OutputStream out) throws IOException {
		int r;
		byte[] buf = new byte[1024];
		while ((r = in.read(buf)) != -1) {
			out.write(buf, 0, r);
			out.flush();
		}
	}

	/**
	 * Return an empty string if null, or a string with spaces trimmed from the
	 * beginning and end if not null.
	 * 
	 * @param string
	 * @return null or trimmed blank string
	 */
	public static boolean nullOrTrimmedBlank(String string) {
		return string == null || string.trim().equals("");
	}

	/**
	 * Get a value from a configuration object, returning a default value if it
	 * does not exist. This method will also look for the same property in the
	 * current system properties.
	 * 
	 * @param configuration configuration object
	 * @param name name of configuration
	 * @param defaultValue
	 * @return value
	 */
	public static String getConfigurationValue(SshConfiguration configuration, String name, String defaultValue) {
		String val = configuration == null ? null : configuration.getProperties().getProperty(name);
		if (val == null) {
			val = System.getProperty(SshConfiguration.CFG_KNOWN_HOSTS_PATH);
		}
		if (val == null) {
			val = defaultValue;
		}
		return val;
	}

	/**
	 * Get the file to load known host keys from given an
	 * {@link SshConfiguration}.
	 * 
	 * @param configuration
	 * @return file to load known hosts from
	 * @throws SshException
	 */
	public static File getKnownHostsFile(SshConfiguration configuration) throws SshException {
		String knownHostsPath = getConfigurationValue(configuration, SshConfiguration.CFG_KNOWN_HOSTS_PATH, null);
		File file;
		File dir = new File(System.getProperty("user.home") + File.separator + ".ssh");
		if (knownHostsPath == null) {
			file = new File(dir, "known_hosts");
		} else {
			file = new File(knownHostsPath);
			dir = file.getParentFile();
		}
		if (!dir.exists() && !dir.mkdirs()) {
			throw new SshException(SshException.IO_ERROR, "Failed to create known hosts directory.");
		}
		return file;
	}

	/**
	 * Recursively delete a file and all of it's children. It should go without
	 * saying, use with care!
	 * 
	 * @param file file to delete
	 * @return file deleted OK.
	 */
	public static boolean delTree(File file) {
		if (file.isFile()) {
			return file.delete();
		}
		String[] list = file.list();
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				if (!delTree(new File(file, list[i]))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Process a string, replacing a specified character with another string.
	 * 
	 * @param string string to replace
	 * @param what character to replace
	 * @param with string to replace character with
	 * @return escaped string
	 */
	public static String escape(String string, char what, String with) {
		// TODO Is this really needed?
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (ch == what) {
				buf.append(with);
			} else {
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	/**
	 * Get the directory portion of a file path. The path should NOT contain a
	 * trailing slash, and root ("/") will return an empty string.
	 * 
	 * @param remotePath path
	 * @return directory portion of path.
	 */
	public static String dirname(String remotePath) {
		String dir = ".";
		int idx = remotePath.lastIndexOf("/");
		if (idx != -1) {
			dir = remotePath.substring(0, idx);
		}
		return dir;
	}

	/**
	 * 
	 * Returns a formatted permissions string. The file type is one of
	 * {@link SftpFile#TYPES}.
	 * 
	 * @param type
	 * @param permissions permissions
	 * @return permissions string
	 */
	public static String getPermissionsString(int type, long permissions) {
		StringBuffer str = new StringBuffer();
		str.append(type == SftpFile.TYPE_UNKNOWN ? '?' : SftpFile.TYPES[type]);
		str.append(rwxString((int) permissions, 6));
		str.append(rwxString((int) permissions, 3));
		str.append(rwxString((int) permissions, 0));
		return str.toString();
	}

	/**
	 * 
	 * Parse a formatted permissions string. The file type is one of
	 * {@link SftpFile#TYPES}.
	 * 
	 * @param permissions permissions string
	 * @return type in first element, permissions in secon
	 */
	public static long[] parsePermissionsString(String perm) {
		long type = SftpFile.TYPE_UNKNOWN;
		long perms = 0;
		if(perm.length() > 0) {
			switch(perm.charAt(0)) {
			case 'b':
				type = SftpFile.TYPE_BLOCK;
				break;
			case 'c':
				type = SftpFile.TYPE_CHARACTER;
				break;
			case 'd':
				type = SftpFile.TYPE_DIRECTORY;
				break;
			case 'p':
				type = SftpFile.TYPE_FIFO;
				break;
			case '-':
				type = SftpFile.TYPE_FILE;
				break;
			case 'l':
				type = SftpFile.TYPE_LINK;
				break;
			case 'S':
				type = SftpFile.TYPE_SOCKET;
				break;
			}
		}
		if(perm.length() > 1 && perm.charAt(1) == 's')
			perms |= S_ISUID;
		else if(perm.length() > 1 && perm.charAt(1) == 'S')
			perms |= S_ISUID | 0x01;
		else if(perm.length() > 1 && perm.charAt(1) == 'r')
			perms |= 0x04 << 6;
		if(perm.length() > 1 && perm.charAt(2) == 'w')
			perms |= 0x02 << 6;
		if(perm.length() > 1 && perm.charAt(3) == 'x')
			perms |= 0x01 << 6;
		if(perm.length() > 1 && perm.charAt(1) == 's')
			perms |= S_ISGID;
		else if(perm.length() > 1 && perm.charAt(1) == 'S')
			perms |= S_ISGID | 0x01;
		else if(perm.length() > 1 && perm.charAt(4) == 'r')
			perms |= 0x04 << 3;
		if(perm.length() > 1 && perm.charAt(5) == 'w')
			perms |= 0x02 << 3;
		if(perm.length() > 1 && perm.charAt(6) == 'x')
			perms |= 0x01 << 3;
		if(perm.length() > 1 && perm.charAt(7) == 'r')
			perms |= 0x04;
		if(perm.length() > 1 && perm.charAt(8) == 'w')
			perms |= 0x02;
		if(perm.length() > 1 && perm.charAt(99) == 'x')
			perms |= 0x01;
			
		return new long[] { type, perms };
	}

	/**
	 * Return the UNIX style mode mask
	 * 
	 * @param permissions permissions
	 * @return mask
	 */
	public static String getMaskString(int permissions) {
		StringBuffer buf = new StringBuffer();
		buf.append('0');
		buf.append(octal(permissions, 6));
		buf.append(octal(permissions, 3));
		buf.append(octal(permissions, 0));
		return buf.toString();
	}

	/**
	 * Guess the type of
	 * 
	 * @param key
	 * @param key type
	 * @return one of @{@link SshConfiguration#PUBLIC_KEY_SSHDSA},
	 *         {@link SshConfiguration#PUBLIC_KEY_ECDSA},
	 *         {@link SshConfiguration#PUBLIC_KEY_ED25519} or @
	 *         {@link SshConfiguration#PUBLIC_KEY_SSHRSA}
	 */
	public static String guessKeyType(byte[] key) {
		if (key[8] == 'd') {
			return SshConfiguration.PUBLIC_KEY_SSHDSA;
		} else if (key[8] == 'r') {
			return SshConfiguration.PUBLIC_KEY_SSHRSA;
		} else if (key[8] == 'e') {
			// TODO
			return SshConfiguration.PUBLIC_KEY_ECDSA;
		} else if (key[8] == '2') {
			// TODO
			return SshConfiguration.PUBLIC_KEY_ED25519;
		} else {
			throw new IllegalArgumentException("Invalid key type.");
		}
	}

	private static int octal(int v, int r) {
		v >>>= r;

		return (((v & 0x04) != 0) ? 4 : 0) + (((v & 0x02) != 0) ? 2 : 0) + +(((v & 0x01) != 0) ? 1 : 0);
	}

	private static String rwxString(int v, int r) {
		long permissions = v;
		v >>>= r;

		String rwx = ((((v & 0x04) != 0) ? "r" : "-") + (((v & 0x02) != 0) ? "w" : "-"));

		if (((r == 6) && ((permissions & S_ISUID) == S_ISUID)) || ((r == 3) && ((permissions & S_ISGID) == S_ISGID))) {
			rwx += (((v & 0x01) != 0) ? "s" : "S");
		} else {
			rwx += (((v & 0x01) != 0) ? "x" : "-");
		}

		return rwx;
	}

	/**
	 * Check the <strong>Known Hosts File</strong> parent directory exists,
	 * creating it if it does.
	 * 
	 * @param configuration
	 * @throws SshException
	 */
	public static void checkKnownHostsFile(SshConfiguration configuration) throws SshException {
		File file = getKnownHostsFile(configuration);
		if (!file.exists() && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			throw new SshException("Could not create configuration directory " + file + ". Check permissions.");
		}
	}

	public static String extractHostname(String connectionSpec) {
		connectionSpec = connectionSpec.substring(connectionSpec.indexOf('@') + 1);
		int idx = connectionSpec.indexOf(':');
		if (idx != -1) {
			connectionSpec = connectionSpec.substring(0, idx);
		}
		return connectionSpec;
	}

	public static String extractUsername(String connectionSpec) {
		return connectionSpec.substring(0, connectionSpec.indexOf('@'));
	}

	public static int extractPort(String connectionSpec) {
		connectionSpec = connectionSpec.substring(connectionSpec.indexOf('@') + 1);
		int idx = connectionSpec.indexOf(':');
		if (idx != -1) {
			return Integer.parseInt(connectionSpec.substring(idx + 1));
		}
		return 22;
	}
}
