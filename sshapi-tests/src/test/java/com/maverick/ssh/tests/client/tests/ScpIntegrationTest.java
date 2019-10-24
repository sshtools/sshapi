package com.maverick.ssh.tests.client.tests;

import static com.maverick.ssh.tests.Util.compare;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientFiles;

import net.sf.sshapi.Capability;
import net.sf.sshapi.SshSCPClient;

public class ScpIntegrationTest extends AbstractClientFiles {
	private SshSCPClient scp;

	@Before
	public final void onConnectedSetUp() throws Exception {
		Assume.assumeTrue("Must support SCP.", ssh.getProvider().getCapabilities().contains(Capability.SCP));
		scp = ssh.scp();
	}

	@After
	public final void onConnectedTearDown() throws Exception {
		if (scp != null) {
			scp.close();
		}
	}

	@Test
	public void testPutThenGet() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support SCP.", ssh.getProvider().getCapabilities().contains(Capability.SCP));
			for (File file : randomFiles.getTestFiles()) {
				scp.put(resolveRemote(file.getName()), "0644", file, false);
				File localFile = new File(randomFiles.getLocalFilesDir(), file.getName());
				scp.get(resolveRemote(file.getName()), localFile, false);
				if (file.length() != localFile.length()) {
					System.out.println("");
				}
				assertEquals("Size of retrieved file must equal size of file sent", file.length(), localFile.length());
				compare(file, localFile);
			}
			return null;
		}, 60000);
	}

	@Test
	public void testPutAndGetRecursive() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support SCP.", ssh.getProvider().getCapabilities().contains(Capability.SCP));
			scp.put(resolveRemote("/"), "0770", randomFiles.getTestFilesDir(), true);
			scp.get(resolveRemote("/"), randomFiles.getLocalFilesDir(), true);
			compare(randomFiles.getTestFilesDir(), randomFiles.getLocalFilesDir(), false);
			return null;
		}, 60000);
	}
}
