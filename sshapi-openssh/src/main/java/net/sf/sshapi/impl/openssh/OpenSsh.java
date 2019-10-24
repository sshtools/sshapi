package net.sf.sshapi.impl.openssh;

public class OpenSsh {

	public static String escape(String command) {
		StringBuffer b = new StringBuffer();
		for(char c : command.toCharArray()) {
			b.append(c);
		}
		return b.toString();
	}
}
