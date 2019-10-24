package com.maverick.ssh.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.sshapi.SshPrivateKey.Algorithm;

public class SshTestConfiguration {
	/**
	 * For SSHAPI client tests, use this one to test against an embedded
	 * Maverick SSHD. All configuration is automatic, these tests should run
	 * "out of the box"
	 * 
	 * Will run on all platforms.
	 */
	public static final String SSHAPI_MAVERICK_CONFIGURATION_NAME = "/sshapi-client-maverick-server.properties";
	/**
	 * For SSHAPI client tests, Use this one when you have a REMOTE (or default
	 * local) OpenSSH server. The server must be configured with the appropriate
	 * test users.
	 * 
	 * Will work on all platforms.
	 */
	public static final String SSHAPI_OPENSSH_CONFIGURATION_NAME = "/sshapi-client-openssh-server.properties";
	/**
	 * For SSHAPI client tests, use this one when you want the tests to
	 * automatically download (and optionally patch) and OpenSSH server. You
	 * must have the GCC, sudo and patch tools installed. The local server must
	 * be configured with the appropriate test users.
	 * 
	 * Will likely only currently work on UNIX like systems such as Linux
	 */
	public static final String SSHAPI_OPENSSH_LOCAL_CONFIGURATION_NAME = "/sshapi-client-openssh-local-server.properties";
	/**
	 * For SSHAPI client tests, use this one to test against an embedded Synergy
	 * SSHD. All configuration is automatic, these tests should run "out of the
	 * box"
	 * 
	 * Will run on all platforms.
	 */
	public static final String SSHAPI_SYNERGY_CONFIGURATION_NAME = "/sshapi-client-synergy-server.properties";
	//
	// VVVVVVVVV Set the default test configuration here VVVVVVVVVVV
	//
	/**
	 * You can either change the default configuration name here or use the
	 * system property sshapiTests.configurationResourceName
	 */
	private static final String DEFAULT_CONFIGURATION_NAME = SSHAPI_OPENSSH_CONFIGURATION_NAME;
	private static SshTestConfiguration instance;
	private static final String KEY_ADMIN_KEY = "adminKey";
	private static final String KEY_ADMIN_USERNAME = "adminUsername";
	private static final String KEY_ALTERNATE_GID = "alternateGid";
	private static final String KEY_ALTERNATE_UID = "alternateUid";
	private static final String KEY_BASIC_COMMAND = "basicCommand";
	private static final String KEY_COMMAND_WITH_INPUT = "commandWithInput";
	private static final String KEY_COMMAND_WITH_INPUT_INPUT = "commandWithInput.input";
	private static final String KEY_COMMAND_WITH_INPUT_PTY_INPUT = "commandWithInputPty.input";
	private static final String KEY_COMMAND_WITH_INPUT_PTY_RESULT = "commandWithInputPty.result";
	private static final String KEY_COMMAND_WITH_INPUT_RESULT = "commandWithInput.result";
	private static final String KEY_COMMAND_WITH_OUTPUT = "commandWithOutput";
	private static final String KEY_COMMAND_WITH_OUTPUT_PTY_RESULT = "commandWithOutputPty.result";
	private static final String KEY_COMMAND_WITH_OUTPUT_RESULT = "commandWithOutput.result";
	private static final String KEY_EXCLUDE_KEY_ALGORITHMS = "excludeKeyAlgos";
	private static final String KEY_FINGERPRINT = "fingerprint";
	private static final String KEY_GID = "gid";
	private static final String KEY_INCLUDE_KEY_ALGORITHMS = "includeKeyAlgos";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_PORT = "port";
	private static final String KEY_PROVIDER = "provider";
	private static final String KEY_SERVER = "server";
	private static final String KEY_SERVER_SERVICE = "serverService";
	private static final String KEY_SUDO_PASSWORD = "sudoPassword";
	private static final String KEY_UID = "uid";
	private static final String KEY_USERNAME = "username";
	public static SshTestConfiguration get() {
		if (instance == null) {
			Properties properties = new Properties();
			try {
				URL resource = SshTestConfiguration.class.getResource("/custom.properties");
				if (resource != null) {
					Properties defaultProperties = new Properties();
					InputStream in = resource.openStream();
					try {
						defaultProperties.load(in);
					} finally {
						in.close();
					}
					properties.putAll(defaultProperties);
				}
				Properties cfgProperties = new Properties();
				String configuration = System.getProperty("sshapiTests.configurationResourceName", DEFAULT_CONFIGURATION_NAME);
				resource = SshTestConfiguration.class.getResource(configuration);
				if (resource == null) {
					throw new FileNotFoundException("Could not find the chosen test configuration file " + configuration);
				}
				InputStream in = resource.openStream();
				try {
					cfgProperties.load(in);
				} finally {
					in.close();
				}
				properties.putAll(cfgProperties);
				String n = resource.getFile();
				int idx = n.lastIndexOf("/");
				String name = n.substring(idx + 1);
				idx = name.lastIndexOf(".");
				name = name.substring(0, idx);
				// Load user properties too
				File file = new File("user.properties");
				if (file.exists()) {
					FileInputStream fin = new FileInputStream(file);
					try {
						Properties userProperties = new Properties();
						userProperties.load(fin);
						properties.putAll(userProperties);
					} finally {
						in.close();
					}
				} else {
					System.err.println("WARNING: no user.properties file found");
				}
				instance = new SshTestConfiguration(name, properties);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
		return instance;
	}
	private List<ServerCapability> capabilties;
	private String name;
	private Properties properties;

	private ServerService server;

	public SshTestConfiguration(String name, Properties properties) {
		this.properties = properties;
		this.name = name;
	}

	public byte[] getAdminKey() {
		try {
			return getOrFail(KEY_ADMIN_KEY).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getAdminUsername() {
		return getOrDefault(KEY_ADMIN_USERNAME, "root");
	}

	public int getAlternateGid() {
		return Integer.parseInt(getOrFail(KEY_ALTERNATE_GID));
	}

	public int getAlternateUid() {
		return Integer.parseInt(getOrFail(KEY_ALTERNATE_UID));
	}

	public String getBasicCommand() {
		return getOrFail(KEY_BASIC_COMMAND);
	}

	public String getChallenge(int number) {
		return getOrFail("prompt." + number + ".challenge");
	}

	public String getCommandWithInput() {
		return getOrFail(KEY_COMMAND_WITH_INPUT);
	}

	public String getCommandWithInputInput() {
		return getOrFail(KEY_COMMAND_WITH_INPUT_INPUT);
	}

	public String getCommandWithInputPtyInput() {
		return getOrFail(KEY_COMMAND_WITH_INPUT_PTY_INPUT);
	}

	public String getCommandWithInputPtyResult() {
		return getOrFail(KEY_COMMAND_WITH_INPUT_PTY_RESULT);
	}

	public String getCommandWithInputResult() {
		return getOrFail(KEY_COMMAND_WITH_INPUT_RESULT);
	}

	public String getCommandWithOutput() {
		return getOrFail(KEY_COMMAND_WITH_OUTPUT);
	}

	public String getCommandWithOutputPtyResult() {
		return getOrFail(KEY_COMMAND_WITH_OUTPUT_PTY_RESULT);
	}

	public String getCommandWithOutputResult() {
		return getOrFail(KEY_COMMAND_WITH_OUTPUT_RESULT);
	}

	public String getFingerprint() {
		return getOrFail(KEY_FINGERPRINT);
	}

	public int getGid() {
		return Integer.parseInt(getOrFail(KEY_GID));
	}

	public List<Algorithm> getKeyAlgorithms() {
		List<Algorithm> l = parseAlgos(KEY_INCLUDE_KEY_ALGORITHMS, Algorithm.algos());
		List<Algorithm> l2 = parseAlgos(KEY_EXCLUDE_KEY_ALGORITHMS);
		List<Algorithm> l3 = new ArrayList<>(l);
		l3.removeAll(l2);
		return l3;
	}

	public String getName() {
		return name;
	}

	public char[] getPassword() {
		return getOrFail(KEY_PASSWORD).toCharArray();
	}

	public int getPort() {
		return Integer.parseInt(getOrFail(KEY_PORT));
	}

	public String getProvider() {
		return getOrDefault(KEY_PROVIDER, "");
	}

	public String getResponse(int number) {
		return getOrFail("prompt." + number + ".response");
	}

	public String getServer() {
		return getOrFail(KEY_SERVER);
	}

	public List<ServerCapability> getServerCapabilities() {
		return capabilties;
	}

	public ServerService getServerService() {
		if (server == null) {
			try {
				server = (ServerService) (Class.forName(getOrFail(KEY_SERVER_SERVICE), true, getClass().getClassLoader())
						.newInstance());
				Properties p = new Properties();
				for (Object key : properties.keySet()) {
					if (key.toString().startsWith(KEY_SERVER_SERVICE + ".")) {
						p.put(key.toString().substring((KEY_SERVER_SERVICE + ".").length()), properties.get(key));
					}
				}
				capabilties = server.init(this, p);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return server;
	}

	public String getSudoPassword() {
		return getOrFail(KEY_SUDO_PASSWORD);
	}

	public int getUid() {
		return Integer.parseInt(getOrFail(KEY_UID));
	}

	public String getUsername() {
		return getOrFail(KEY_USERNAME);
	}

	public void setFingerprint(String fingerprint) {
		properties.put(KEY_FINGERPRINT, fingerprint);
	}

	public void setPort(int port) {
		properties.put(KEY_PORT, String.valueOf(port));
	}

	private String getOrDefault(String key, String defaultValue) {
		String v = System.getProperty(key);
		if (v == null) {
			v = properties.getProperty(key);
		}
		if (v == null) {
			return defaultValue;
		}
		return v;
	}

	private String getOrFail(String key) {
		String v = System.getProperty(key);
		if (v == null) {
			v = properties.getProperty(key);
		}
		if (v == null) {
			throw new RuntimeException("Missing configuratoin " + key);
		}
		return v;
	}

	private List<Algorithm> parseAlgos(String key, Algorithm... def) {
		List<Algorithm> l = new ArrayList<Algorithm>();
		String[] v = getOrDefault(key, "").split(",");
		if (v.length == 0 || (v.length == 1 && v[0].equals("")))
			l.addAll(Arrays.asList(def));
		else {
			for (String a : v) {
				l.add(Algorithm.fromAlgoName(a));
			}
		}
		return l;
	}
}
