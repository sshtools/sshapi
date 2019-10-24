package com.maverick.ssh.tests.client.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.maverick.ssh.tests.client.AbstractClientConnected;

import net.sf.sshapi.SshCommand;

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
				assertTrue(String.format("Start of output [%s] from command should match that expected [%s]", res, config.getCommandWithOutputResult()), 
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
				assertTrue(String.format("Start of output [%s] from command should match that expected [%s]", res, config.getCommandWithOutputPtyResult()), 
						res.startsWith(config.getCommandWithOutputPtyResult()));
			}
			return null;
		}, 30000);
	}

	@Test
	public void testExecuteWithInput() throws Exception {
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
			try (SshCommand command = ssh.command(config.getCommandWithInput())) {
				// Send. Note, must close the stream to receive the input
				try (OutputStream outputStream = command.getOutputStream()) {
					outputStream.write(config.getCommandWithInputPtyInput().getBytes());
					outputStream.flush();
				}
				// Read result
				assertEquals("Output from command should match that expected", config.getCommandWithInputPtyResult(),
						IOUtils.toString(command.getInputStream(), "UTF-8"));
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
