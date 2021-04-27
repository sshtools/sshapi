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
				LOG.info("Copying {0} to {1} ({2} bytes)", file, resolveRemote(file.getName()), file.length());
				scp.put(resolveRemote(file.getName()), "0644", file, false);
				File localFile = new File(randomFiles.getLocalFilesDir(), file.getName());
				LOG.info("Retrieving {0} from {1} ({2} bytes)", file, resolveRemote(file.getName()), file.length());
				scp.get(resolveRemote(file.getName()), localFile, false);
				assertEquals("Size of retrieved file must equal size of file sent", file.length(), localFile.length());
				LOG.info("Comparing {0} and {1}", file, localFile);
				compare(file, localFile);
				break;
			}
			return null;
		}, 240000);
	}

	@Test
	public void testPutAndGetRecursive() throws Exception {
		timeout(() -> {
			Assume.assumeTrue("Must support SCP.", ssh.getProvider().getCapabilities().contains(Capability.SCP));
			Assume.assumeTrue("Must support SCP recursive get.", ssh.getProvider().getCapabilities().contains(Capability.RECURSIVE_SCP_GET));
			scp.put(resolveRemote("/"), "0770", randomFiles.getTestFilesDir(), true);
			scp.get(resolveRemote("/"), randomFiles.getLocalFilesDir(), true);
			compare(randomFiles.getTestFilesDir(), randomFiles.getLocalFilesDir(), false);
			return null;
		}, 120000);
	}
}
