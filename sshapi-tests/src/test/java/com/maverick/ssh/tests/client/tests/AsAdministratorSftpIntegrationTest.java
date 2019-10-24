package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientSftp;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.util.DefaultPublicKeyAuthenticator;

public class AsAdministratorSftpIntegrationTest extends AbstractClientSftp {
	@Test
	public void testChown() throws Exception {
		timeout(() -> {
			createFile("testChownFile");
			sftp.chown(resolveRemote("testChownFile"), config.getAlternateUid());
			assertEquals("Uid should now be the alternate", config.getAlternateUid(),
					Integer.valueOf(sftp.stat(resolveRemote("testChownFile")).getUID()).intValue());
			return null;
		}, 10000);
	}

	@Override
	protected SshAuthenticator createAuthenticator() {
		return new DefaultPublicKeyAuthenticator(config.getAdminKey());
	}

	@Override
	protected String getUsername() {
		return config.getAdminUsername();
	}
}
