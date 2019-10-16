/* 
 * Copyright (c) 2010 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;

import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;

/**
 * Client implementations are responsible for maintaining the connection with
 * the server, authentication, and access to the sub-systems
 */
public interface SshClient extends Closeable, AutoCloseable {

	/**
	 * Get the configuration this client is using
	 * 
	 * @return configuration
	 */
	SshConfiguration getConfiguration();

	/**
	 * Called after instantiation to initialise the client and supply the provider
	 * that created it. This method should not be be called directly.
	 * 
	 * @param provider
	 *            provider
	 */
	void init(SshProvider provider);

	/**
	 * Get the provider that created this client.
	 * 
	 * @return provider
	 */
	SshProvider getProvider();

	/**
	 * Connect to specified host and port using the provided username, and
	 * optionally authenticate if you provide one of more authenticate. If you do
	 * not provide an authenticator, you must call
	 * {@link #authenticate(SshAuthenticator)}. If authentication fails the first
	 * time, it will be reattempted until
	 * {@link SshConfiguration#getMaxAuthAttempts()}] is reached.
	 * 
	 * @param spec
	 *            connection spec in the format username[:password]@host[:port]
	 * @param authenticators
	 *            authenticators
	 * @throws SshException
	 *             on any error
	 */
	void connect(String spec, SshAuthenticator... authenticators) throws SshException;

	/**
	 * Connect to specified host and port using the provided username, and
	 * optionally authenticate if you provide one of more authenticate. If you do
	 * not provide an authenticator, you must call
	 * {@link #authenticate(SshAuthenticator)}. If authentication fails the first
	 * time, it will be reattempted until
	 * {@link SshConfiguration#getMaxAuthAttempts()}] is reached.
	 * 
	 * @param username
	 *            user name
	 * @param hostname
	 *            host name
	 * @param port
	 *            port
	 * @param authenticators
	 *            authenticators
	 * @throws SshException
	 *             on any error
	 */
	void connect(String username, String hostname, int port, SshAuthenticator... authenticators) throws SshException;

	/**
	 * Authenticate. An authenticator should be provided for each type supported.
	 * This method may be called multiple times until <code>true</code> is return or
	 * an exception is thrown. {@link SshConfiguration#getMaxAuthAttempts()}] has no
	 * impact on this method.
	 * 
	 * @param authenticators
	 *            authenticators. At least one must be provided
	 * @return <code>true</code> when full authenticated, or <code>false</code> if
	 *         more authentication is required
	 * @throws SshException
	 */
	boolean authenticate(SshAuthenticator... authenticators) throws SshException;

	/**
	 * Create a new shell.
	 * <p>
	 * Note, this method does not actually start the shell, you must call
	 * {@link SshShell#open()} to do that.
	 * <p>
	 * Remember to close the shell when you are finished with it using
	 * {@link SshShell#close()}, or use a try-with-resource.
	 * 
	 * @param termType
	 *            terminal type, or use <code>null</code> to NOT request a pseudo
	 *            terminal
	 * @param cols
	 *            width of terminal in characters (use zero if not pseudo terminal
	 *            should be requested)
	 * @param rows
	 *            height of terminal in characters (use zero if not pseudo terminal
	 *            should be requested)
	 * @param pixWidth
	 *            width of terminal in pixels (if known, otherwise use zero)
	 * @param pixHeight
	 *            height of terminal in pixels (if known, otherwise use zero)
	 * @param terminalModes
	 *            terminal modes (or null or empty array)
	 * @return shell
	 * @throws SshException
	 * @see {@link #shell(String, int, int, int, int, byte[])}}
	 */
	SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException;

	/**
	 * <p>
	 * Create a new shell and open it. This method is intended for simpler cases and
	 * is at it's best when used with try-with-resource. E.g. <code>
	 * <pre>
	 * try(client.shell("vt100",80,24,0,0,null)) {
	 * 	// Do stuff. The channel will be automatically closed when leaving the scope of the try.
	 * }
	 * </pre>
	 * </code>
	 * </p>
	 * <p>
	 * Note, this method does not give you the opportunity to add any listeners
	 * before the shell is started, so data may be missed if you wish to use
	 * listeners. In this case, use
	 * {@link #createShell(String, int, int, int, int, byte[])} instead.
	 * </p>
	 * <p>
	 * Remember to close the shell when you are finished with it using
	 * {@link SshShell#close()}, or use a try-with-resource.
	 * 
	 * @param termType
	 *            terminal type, or use <code>null</code> to NOT request a pseudo
	 *            terminal
	 * @param cols
	 *            width of terminal in characters (use zero if not pseudo terminal
	 *            should be requested)
	 * @param rows
	 *            height of terminal in characters (use zero if not pseudo terminal
	 *            should be requested)
	 * @param pixWidth
	 *            width of terminal in pixels (if known, otherwise use zero)
	 * @param pixHeight
	 *            height of terminal in pixels (if known, otherwise use zero)
	 * @param terminalModes
	 *            terminal modes (or null or empty array)
	 * @return shell
	 * @throws SshException
	 * @see {@link #createShell(String, int, int, int, int, byte[])}
	 */
	SshShell shell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException;

	/**
	 * Create a local forward. Local forwards create a listening socket on the
	 * client that is tunneled through to the remote server which then makes the
	 * connection to the target.
	 * <p>
	 * Once you have created the {@link SshPortForward} object, you must start
	 * tunnel using {@link SshPortForward#open()}. When you have finished with the
	 * tunnel, call {@link SshPortForward#close()} (closed tunnels may be re-opened
	 * by calling the {@link SshPortForward#open()} method again).
	 * <p>
	 * If your use-case is simple, you may want to consider
	 * {@link #remoteForward(String, int, String, int)} instead that opens the
	 * forward for you as fits better to with try-with-resource.
	 * 
	 * @param localBindAddress
	 *            local address to bind to. Use <code>null</code> to bind to all
	 *            addresses. Note, this may not be supported by all implementations.
	 * @param localBindPort
	 *            local port to listen on
	 * @param targetAddress
	 *            remote host to tunnel to
	 * @param targetPort
	 *            remote port to tunnel to
	 * @return local forward
	 * @throws SshException
	 *             on any error
	 * @see {@link #localForward(String, int, String, int)}
	 */
	SshPortForward createLocalForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException;

	/**
	 * Create and open a local forward. Local forwards create a listening socket on
	 * the client that is tunneled through to the remote server which then makes the
	 * connection to the target.
	 * <p>
	 * Note, this method does not give you the opportunity to add any listeners
	 * before the tunnel is started, so data may be missed if you wish to use
	 * listeners. In this case, use
	 * {@link #createLocalForward(String, int, String, int)} instead.
	 * 
	 * @param localBindAddress
	 *            local address to bind to. Use <code>null</code> to bind to all
	 *            addresses. Note, this may not be supported by all implementations.
	 * @param localBindPort
	 *            local port to listen on
	 * @param targetAddress
	 *            remote host to tunnel to
	 * @param targetPort
	 *            remote port to tunnel to
	 * @return local forward
	 * @throws SshException
	 *             on any error
	 * @see {@link #createLocalForward(String, int, String, int)}
	 */
	SshPortForward localForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException;

	/**
	 * Create a remote forward. Remote forwards create a listening socket on the SSH
	 * server you are connected to, and tunnel connections back to the client end
	 * and then on to the target.
	 * <p>
	 * Once you have created the {@link SshPortForward} object, you must start
	 * tunnel using {@link SshPortForward#open()}. When you have finished with the
	 * tunnel, call {@link SshPortForward#close()} (closed tunnels may be re-opened
	 * by calling the {@link SshPortForward#open()} method again).
	 * <p>
	 * If your use-case is simple, you may want to consider
	 * {@link #remoteForward(String, int, String, int)} instead that opens the
	 * forward for you as fits better to with try-with-resource.
	 * 
	 * @param remoteBindAddress
	 *            the address on the remote server to listen. This must either be
	 *            <code>null</code> to bind to all address on that server, or a
	 *            single address that exists on that server (including 127.0.0.1).
	 * @param remoteBindPort
	 *            remote port to tunnel to
	 * @param targetAddress
	 *            local address to bind to. Use <code>null</code> to bind to all
	 *            addresses. Note, this may not be supported by all implementations.
	 * @param targetPort
	 *            local port to listen on
	 * @return local forward
	 * @throws SshException
	 *             on any error
	 * @see {@link #remoteForward(String, int, String, int)}}
	 */
	SshPortForward createRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException;

	/**
	 * Create a remote forward and open it. Remote forwards create a listening
	 * socket on the SSH server you are connected to, and tunnel connections back to
	 * the client end and then on to the target.
	 * <p>
	 * Note, this method does not give you the opportunity to add any listeners
	 * before the tunnel is started, so data may be missed if you wish to use
	 * listeners. In this case, use
	 * {@link #createRemoteForward(String, int, String, int)} instead.
	 * 
	 * 
	 * @param remoteBindAddress
	 *            the address on the remote server to listen. This must either be
	 *            <code>null</code> to bind to all address on that server, or a
	 *            single address that exists on that server (including 127.0.0.1).
	 * @param remoteBindPort
	 *            remote port to tunnel to
	 * @param targetAddress
	 *            local address to bind to. Use <code>null</code> to bind to all
	 *            addresses. Note, this may not be supported by all implementations.
	 * @param targetPort
	 *            local port to listen on
	 * @return local forward
	 * @throws SshException
	 *             on any error
	 * @see {@link #createRemoteForward(String, int, String, int)}}
	 */
	SshPortForward remoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress, int targetPort)
			throws SshException;

	/**
	 * Execute a command on the remote server.
	 * <p>
	 * Note, this method does not actually start the command and open the channel,
	 * you must call {@link SshLifecycleComponent#open()} to do that.
	 * <p>
	 * Remember to close the channel when you are finished with it using
	 * {@link SshLifecycleComponent#close()}.
	 * 
	 * @param command
	 *            command to execute
	 * @return channel
	 * @throws SshException
	 *             on any error
	 */
	SshCommand createCommand(String command) throws SshException;

	/**
	 * 
	 * <p>
	 * Execute a command on the remote server, open the channel. This method is
	 * intended for simpler cases and is at it's best when used with
	 * try-with-resource. E.g. <code>
	 * <pre>
	 * try(client.command("ls /etc")) {
	 * 	// Do stuff. The channel will be automatically closed when leaving the scope of the try.
	 * }
	 * </pre>
	 * </code>
	 * </p>
	 * <p>
	 * Note, this method does not give you the opportunity to add any listeners
	 * before the shell is started, so data may be missed if you wish to use
	 * listeners. In this case, use {@link #createCommand(String)} instead.
	 * </p>
	 * <p>
	 * Remember to close the channel when you are finished with it using
	 * {@link SshLifecycleComponent#close()} or use try-with-resource.
	 * 
	 * @param command
	 *            command to execute
	 * @return channel
	 * @throws SshException
	 *             on any error
	 */
	SshCommand command(String command) throws SshException;

	/**
	 * Create a {@link SocketFactory} whose connections are actually tunneled over
	 * this SSH connection and made from the remote machine. This actually makes use
	 * of local port forward, but instead of setting up a local server socket, the
	 * tunnels are accessable as {@link Socket} objects.
	 * 
	 * @return tunneled socket factory
	 * @throws SshException
	 */
	SocketFactory createTunneledSocketFactory() throws SshException;

	/**
	 * Create a new SCP client. This may be used for simple file transfer. For a
	 * more fully featured file-system like approach, use {@link #createSftpClient}.
	 * 
	 * @return scp client
	 * @throws SshException
	 *             on any error
	 * @see {@link #createSCP()}
	 */
	@Deprecated
	SshSCPClient createSCPClient() throws SshException;

	/**
	 * Create a new SCP client. This may be used for simple file transfer. For a
	 * more fully featured file-system like approach, use {@link #createSftpClient}.
	 * <p>
	 * Once you have created the {@link SshSCPClient} object, you must start SCP
	 * using {@link SshSCPClient#open()}. When you have finished with the SCP, call
	 * {@link SshSCPClient#close()}.
	 * <p>
	 * Remember to close the client when you are finished with it using
	 * {@link SshSCPClient#close()} or use try-with-resource.
	 * <p>
	 * If your use-case is simple, you may want to consider {@link #scp()} instead
	 * that opens the client for you as fits better to with try-with-resource.
	 * 
	 * @return scp client
	 * @throws SshException
	 *             on any error
	 * @see {@link #scp()}
	 */
	SshSCPClient createSCP() throws SshException;

	/**
	 * Create a new SCP client and start it. This may be used for simple file
	 * transfer. For a more fully featured file-system like approach, use
	 * {@link #createSftpClient}.
	 * <p>
	 * Remember to close the client when you are finished with it using
	 * {@link SshSCPClient#close()} or use try-with-resource.
	 * 
	 * @return scp client
	 * @throws SshException
	 *             on any error
	 * @see {@link #createSCP()}
	 */
	SshSCPClient scp() throws SshException;

	/**
	 * Create a new SFTP client that may be used to perform file operations.
	 * <p>
	 * Note, this method does not actually start the client, you must call
	 * {@link SshLifecycleComponent#open()} to do that.
	 * <p>
	 * Remember to close the client when you are finished with it using
	 * {@link SshLifecycleComponent#close()}.
	 * 
	 * @return SFTP client
	 * @throws SshException
	 * @see {@link #createSftp()}
	 */
	@Deprecated
	SftpClient createSftpClient() throws SshException;

	/**
	 * Create a new SFTP client that may be used to perform file operations.
	 * <p>
	 * Note, this method does not actually start the client, you must call
	 * {@link SshLifecycleComponent#open()} to do that.
	 * <p>
	 * Remember to close the client when you are finished with it using
	 * {@link SshLifecycleComponent#close()} or use try-with-resource.
	 * <p>
	 * If your use-case is simple, you may want to consider {@link #sftp()} instead
	 * that opens the forward for you as fits better to with try-with-resource.
	 * 
	 * @return SFTP client
	 * @throws SshException
	 * @see {@link #sftp()}
	 */
	SftpClient createSftp() throws SshException;

	/**
	 * Create a new SFTP client that may be used to perform file operations and
	 * start it.
	 * <p>
	 * Remember to close the client when you are finished with it using
	 * {@link SshLifecycleComponent#close()} or use try-with-resource.
	 * 
	 * @return SFTP client
	 * @throws SshException
	 * @see {@link #createSftp()}
	 */
	SftpClient sftp() throws SshException;

	/**
	 * Create an instance of a {@link SshPublicKeySubsystem}, if the provider
	 * supports it.
	 * <p>
	 * Note, this method does not actually start the subsystems, you must call
	 * {@link SshPublicKeySubsystem#open()} to do that.
	 * <p>
	 * Remember to close the subsystem when you are finished with it using
	 * {@link SshPublicKeySubsystem#close()}.
	 * <p>
	 * If your use case is simple, consider {@link #publicKeySubsystem()} instead
	 * that opens it for you.
	 * 
	 * @return public key subsystem instance.
	 * @throws SshException
	 * @throws UnsupportedOperationException
	 *             if no supported
	 * @see {@link #publicKeySubsystem()}
	 */
	SshPublicKeySubsystem createPublicKeySubsystem() throws SshException;

	/**
	 * Create an instance of a {@link SshPublicKeySubsystem}, if the provider
	 * supports it, and open it.
	 * <p>
	 * Remember to close the subsystem when you are finished with it using
	 * {@link SshLifecycleComponent#close()}.
	 * 
	 * @return public key subsystem instance.
	 * @throws SshException
	 * @throws UnsupportedOperationException
	 *             if no supported
	 * @see {@link #createPublicKeySubsystem()}
	 */
	SshPublicKeySubsystem publicKeySubsystem() throws SshException;

	/**
	 * Disconnect this client.
	 * 
	 * @throws SshException
	 * @see {@link #close()}
	 * @deprecated
	 */
	void disconnect() throws SshException;

	/**
	 * Close this client, but don't complain if it is not connected or there are
	 * other errors.
	 */
	void closeQuietly();

	/**
	 * Get if the client is currently connected. A client may be connected, but not
	 * yet authenticated.
	 * 
	 * @return connected
	 * @see #isAuthenticated()
	 */
	boolean isConnected();

	/**
	 * Get if the client is currently authenticated. This implies the client is
	 * connected.
	 * 
	 * @return authenticated
	 * @see #isConnected()
	 */
	boolean isAuthenticated();

	/**
	 * Get the remote server identification string. Client must be connected.
	 * 
	 * @return remote identification
	 * @throws IllegalStateException
	 *             if not connected
	 */
	String getRemoteIdentification();

	/**
	 * Get the protocol version. Will be one of {@link SshConfiguration#SSH1_ONLY}
	 * or {@link SshConfiguration#SSH2_ONLY}.
	 * 
	 * @return protocol version
	 * @throws IllegalStateException
	 *             if not connected
	 */
	int getRemoteProtocolVersion();

	/**
	 * Get the currently authenticated user.
	 * 
	 * @return user
	 */
	String getUsername();

	/**
	 * Get the number of channels that are currently open.
	 * 
	 * @return open channels
	 * @throws IllegalStateException
	 *             if not connected
	 */
	int getChannelCount();

	/**
	 * Add a listener that is notified when channels are opened on this port
	 * forward.
	 * 
	 * @param listener
	 *            listener to add
	 */
	void addPortForwardListener(SshPortForwardListener listener);

	/**
	 * Remove a listener that is notified when channels are opened on this port
	 * forward.
	 * 
	 * @param listener
	 *            listener to remove
	 */
	void removePortForwardListener(SshPortForwardListener listener);

	/**
	 * Set the timeout on the underlying transport (usually a Socket, if supported).
	 * A timeout of zero indicates infinite timeout
	 * 
	 * @param timeout
	 *            timeout
	 * @throws IOException
	 */
	void setTimeout(int timeout) throws IOException;

	/**
	 * Get the timeout on the underlying transport (usually a Socket, if supported).
	 * A timeout of zero indicates infinite timeout
	 * 
	 * @return timeout
	 * @throws IOException
	 */
	int getTimeout() throws IOException;

	/**
	 * Adds a channel handler to those invoked when custom channel creation requests
	 * are received.
	 * 
	 * @param channelHandler
	 *            channel handler to add
	 * @throws SshException
	 */
	void addChannelHandler(SshChannelHandler channelHandler) throws SshException;

	/**
	 * Remove a channel handler from the list of those invoked when custom channel
	 * creation requests are received.
	 * 
	 * @param channelHandler
	 *            channel handler to remove
	 * @throws SshException
	 */
	void removeChannelHandler(SshChannelHandler channelHandler) throws SshException;
}
