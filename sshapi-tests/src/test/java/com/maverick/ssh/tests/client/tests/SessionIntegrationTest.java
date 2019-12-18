package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnected;
import com.maverick.ssh.tests.client.util.DataEventCapture;

import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;

/**
 * <p>
 * This test executes a commands with variations of whether it is a simple
 * command with no input or output, a command with just output or a command with
 * both input and output.
 * 
 * <p>
 * Tests are also made with a PTY being allocated and a PTY not being allocated.
 * The output and input characteristics will be different as the host may buffer
 * input or have additional input or output processing. For example, when a PTY
 * is allocated input streams are stopped with a Ctrl+bD and output will have
 * CRLF sequences instead of just LF
 *
 */
public class SessionIntegrationTest extends AbstractClientConnected {

	@Test
	public void testShellNoPty() throws Exception {
		timeout(() -> {
			return doEventTest(ssh.createShell());
		}, TimeUnit.SECONDS.toMillis(60));
	}

	@Test
	public void testShellPty() throws Exception {
		timeout(() -> {
			return doEventTest(ssh.createShell("vt100", 80, 24, 0, 0, null));
		}, 30000);
	}

	private Void doEventTest(SshShell cmd) throws SshException, InterruptedException, IOException {
		DataEventCapture<SshShell> cap = new DataEventCapture<SshShell>();
		cmd.addDataListener(cap);
		cmd.addListener(cap);
		cmd.open();
		Thread t = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Semaphore sem = new Semaphore(1);
		sem.acquire();

		/*
		 * Interrupt in 10 seconds, should be more than enough time to read in initial
		 * output. Closing the channel will break the streams so the read thread exits
		 * and continues. To avoid attempting to close twice we keep track of when the
		 * stream has been closed
		 */
		t = new Thread("SessionIntegegrationTestCloser") {
			public void run() {
				try {
					Thread.sleep(10000);
					cmd.closeQuietly();
				} catch (Exception e) {
				} finally {
					sem.release();
				}
			}
		};
		t.setDaemon(true);
		t.start();

		/*
		 * Read the bytes to increase data in. Also read in stderr
		 */

		Thread t2 = new Thread("SessionIntegegrationTestStdErrReader") {
			public void run() {
				try {
					IOUtils.copy(cmd.getExtendedInputStream(), baos);
				} catch (Exception e) {
				} finally {
				}
			}
		};
		try {
			t2.setDaemon(true);
			t2.start();

			IOUtils.copy(cmd.getInputStream(), baos);
			sem.acquire();
			sem.release();

			int length = baos.toByteArray().length;
			Assert.assertNotEquals("Bytes received", 0, length);
			cap.assertEvents(1, 1, 1, 1, length, 0, 0);
			return null;
		} finally {
			// Just in case
			t2.interrupt();
		}
	}

	@Test
	public void testCommandOutputEvents() throws Exception {
		timeout(() -> {
			SshCommand cmd = ssh.createCommand(config.getCommandWithOutput());
			DataEventCapture<SshCommand> cap = new DataEventCapture<SshCommand>();
			cmd.addDataListener(cap);
			cmd.addListener(cap);
			cmd.open();
			try {
				/* Read the bytes to increase data in */
				IOUtils.toString(cmd.getInputStream(), "UTF-8");
			} finally {
				cmd.close();
			}
			cap.assertEvents(1, 1, 1, 1, 54, 0, 0);
			return null;
		}, 30000);
	}

	@Test
	public void testCommandInputOutputEvents() throws Exception {
		timeout(() -> {
			SshCommand cmd = ssh.createCommand(config.getCommandWithInput());
			DataEventCapture<SshCommand> cap = new DataEventCapture<SshCommand>();
			cmd.addDataListener(cap);
			cmd.addListener(cap);
			cmd.open();
			try {
				try (OutputStream outputStream = cmd.getOutputStream()) {
					outputStream.write(config.getCommandWithInputInput().getBytes());
					outputStream.flush();
				}
				IOUtils.toString(cmd.getInputStream(), "UTF-8");
			} finally {
				cmd.close();
			}
			cap.assertEvents(1, 1, 1, 1, 12, 45, 0);
			return null;
		}, 30000);
	}

	@Test
	public void testExecute() throws Exception {
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getBasicCommand())) {
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecutePty() throws Exception {
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getBasicCommand(), "dumb", 0, 0, 0, 0, null)) {
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteWithOutput() throws Exception {
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getCommandWithOutput())) {
				String res = IOUtils.toString(command.getInputStream(), "UTF-8");
				assertTrue(
						String.format("Start of output [%s] from command should match that expected [%s]", res,
								config.getCommandWithOutputResult()),
						res.startsWith(config.getCommandWithOutputResult()));
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteWithOutputPty() throws Exception {
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getCommandWithOutput(), "dumb", 0, 0, 0, 0, null)) {
				String res = IOUtils.toString(command.getInputStream(), "UTF-8");
				assertTrue(
						String.format("Start of output [%s] from command should match that expected [%s]", res,
								config.getCommandWithOutputPtyResult()),
						res.startsWith(config.getCommandWithOutputPtyResult()));
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteWithInput() throws Exception {
//		Assume.assumeFalse("Must not be Maverick Synergy, currently hangs entirely",ssh.getProvider().getName().equals("Maverick Synergy"));
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getCommandWithInput())) {
				// Send. Note, must close the stream to receive the input
				try (OutputStream outputStream = command.getOutputStream()) {
					outputStream.write(config.getCommandWithInputInput().getBytes());
					outputStream.flush();
				}
				// Read result
				assertEquals("Output from command should match that expected", config.getCommandWithInputResult(),
						IOUtils.toString(command.getInputStream(), "UTF-8"));
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteWithInputPty() throws Exception {
		timeout(() -> {
			try (SshCommand command = ssh.command(config.getCommandWithInput(), "dumb", 80, 24, 0, 0, null)) {
				// Send. Note, must close the stream to receive the input
				try (OutputStream outputStream = command.getOutputStream()) {
					String str = config.getCommandWithInputPtyInput();
					byte[] bytes = str.getBytes();
					outputStream.write(bytes);
					outputStream.flush();
				}
				// Read result
				String str2 = IOUtils.toString(command.getInputStream(), "UTF-8");
				String str1 = config.getCommandWithInputPtyResult();
				assertEquals("Output from command should match that expected", str1, str2);
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteFail() throws Exception {
		timeout(() -> {
			SshCommand command = ssh.command("XXXXXXXXXXXXXXXXXXXXX");
			command.close();
			assertNotEquals("Command exit value should be non-zero", 0, command.exitCode());
			return null;
		}, 30000);
	}
}
