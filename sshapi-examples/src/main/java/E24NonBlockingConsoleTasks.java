import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import net.sf.sshapi.Ssh;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.util.ConsoleBannerHandler;
import net.sf.sshapi.util.ConsoleHostKeyValidator;
import net.sf.sshapi.util.ConsolePasswordAuthenticator;
import net.sf.sshapi.util.Util;

/**
 * Similar to link {@link E23NonBlockingConsole}, but instead
 * {@link Ssh#then(java.util.concurrent.Future, net.sf.sshapi.Ssh.SshCallable)}
 * is used to demonstrate a sequence of chained tasks.
 */
public class E24NonBlockingConsoleTasks {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration().setHostKeyValidator(new ConsoleHostKeyValidator())
				.setBannerHandler(new ConsoleBannerHandler());
		Ssh.then(config.openLater(Util.promptConnectionSpec()), (client) -> {
			ExampleUtilities.dumpClientInfo(client);
			System.out.println("Remote identification: " + client.getRemoteIdentification());
			Ssh.then(client.authenticateLater(new ConsolePasswordAuthenticator()), (authenticated) -> {
				if (!authenticated) {
					throw new SshException(SshException.AUTHENTICATION_FAILED);
				}
				/*
				 * Now get the shell. Because we want to set input handlers, we
				 * don't use client.shellLater(), instead we create the shell,
				 * set the handlers then open it
				 */
				SshShell shell = client.createShell("dumb", 80, 24, 0, 0, null);
				/*
				 * Set the handlers that are invoked when data arrives on shell
				 * in or shell err
				 **/
				shell.setInput((buffer) -> System.out.write(buffer.array(), 0, buffer.limit()));
				shell.setErrInput((buffer) -> System.err.write(buffer.array(), 0, buffer.limit()));
				Ssh.then(shell.openLater(), (v) -> {
					/*
					 * Read from console and write to shell out. We have to
					 * translate between console I/O streams and NIO buffer
					 */
					byte[] buf = new byte[1024];
					int r;
					while ((r = System.in.read(buf)) != -1)
						shell.writeLater(ByteBuffer.wrap(buf, 0, r));
				});
			});
		}).closeLater().get(1, TimeUnit.MINUTES);
	}
}
