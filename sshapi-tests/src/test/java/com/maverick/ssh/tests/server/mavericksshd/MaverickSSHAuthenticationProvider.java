package com.maverick.ssh.tests.server.mavericksshd;

import java.io.File;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.maverick.ssh.tests.SshTestConfiguration;
import com.maverick.sshd.Connection;
import com.maverick.sshd.platform.AuthenticationProvider;

public class MaverickSSHAuthenticationProvider implements
		AuthenticationProvider {

	class User {
		private String username;
		@SuppressWarnings("unused")
		private int gid;
		@SuppressWarnings("unused")
		private int uid;
		private char[] password;

		public User(String username, int gid, int uid, char[] password) {
			super();
			this.username = username;
			this.gid = gid;
			this.uid = uid;
			this.password = password;
		}

	}

	private Map<String, User> users = new HashMap<String, User>();
	private File homeRoot;

	public MaverickSSHAuthenticationProvider(SshTestConfiguration configuration) {
		addUser(new User("root", 0,
				0, configuration.getPassword()));
		addUser(new User("testuser", configuration.getUid(),
				configuration.getGid(), configuration.getPassword()));
		addUser(new User("testuser2", configuration.getAlternateUid(),
				configuration.getAlternateGid(), configuration.getPassword()));
		homeRoot = new File(new File(System.getProperty("java.io.tmpdir"),
				"maverick-sshd-homes"), "home");
	}

	public boolean changePassword(Connection con, String username,
			String oldpassword, String newpassword) {
		User user = users.get(username);
		if (user == null
				|| (user.password != null && !new String(user.password)
						.equals(oldpassword))) {
			return false;
		}
		user.password = newpassword.toCharArray();
		return true;
	}

	public boolean verifyPassword(Connection con, String username,
			String password, SocketAddress ipAddress)
			throws com.maverick.sshd.platform.PasswordChangeException {
		User user = users.get(username);
		if (user != null
				&& (user.password == null || new String(user.password)
						.equals(password))) {
			return true;
		}
		return false;
	}


	public String getGroup(Connection con) {
		return "users";
	}

	public String getHomeDirectory(Connection con) {
		String username = con.getUsername();
		if (!users.containsKey(con.getUsername())) {
			username = "_guest_";
		}
		File file = new File(homeRoot, username);
		file.mkdirs();
		return file.getAbsolutePath();
	}

	private void addUser(User user) {
		users.put(user.username, user);
	}
	
	public void startSession(Connection con) {
		
	}

	public void endSession(Connection con) {
		
	}

}
