package com.maverick.ssh.tests;

import java.util.List;
import java.util.Properties;

public interface ServerService {
	
	public enum AuthenticationMethod {
		PASSWORD, PUBLICKEY, KEYBOARD_INTERACTIVE;
		
		public String toString() {
			return name().toLowerCase().replace("_", "-");
		}
	}
	
	void addRequiredAuthentication(AuthenticationMethod method);
	void removeRequiredAuthentication(AuthenticationMethod method);
	
	List<ServerCapability> init(SshTestConfiguration configuration, Properties serviceProperties) throws Exception;
	void start() throws Exception;
	void stop() throws Exception;
	void restart() throws Exception;
	
}
