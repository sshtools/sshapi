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
package net.sf.sshapi.impl.nassh;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.SocketFactory;

import org.snf4j.core.EndingAction;
import org.snf4j.core.StreamSession;
import org.snf4j.core.future.FailedFuture;
import org.snf4j.core.handler.AbstractStreamHandler;
import org.snf4j.core.handler.SessionEvent;
import org.snf4j.core.session.DefaultSessionConfig;
import org.snf4j.core.session.ISessionConfig;

import net.sf.sshapi.AbstractBaseClient;
import net.sf.sshapi.SshChannelHandler;
import net.sf.sshapi.SshClientListener;
import net.sf.sshapi.SshCommand;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshCustomChannel;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshLifecycleComponent;
import net.sf.sshapi.SshSCPClient;
import net.sf.sshapi.SshShell;
import net.sf.sshapi.auth.SshAuthenticator;
import net.sf.sshapi.forwarding.SshPortForward;
import net.sf.sshapi.forwarding.SshPortForwardListener;
import net.sf.sshapi.identity.SshPublicKeySubsystem;
import net.sf.sshapi.sftp.SftpClient;

class NaSshClient extends AbstractBaseClient {

	private SocketChannel socketChannel;
	private Queue<ByteBuffer> pendingData = new LinkedList<>();

	public NaSshClient(SshConfiguration configuration) {
		super(configuration);
	}

	@Override
	public boolean authenticate(SshAuthenticator... authenticators) throws SshException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConnected() {
		return socketChannel != null && socketChannel.isConnected();
	}

	@Override
	public boolean isAuthenticated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getRemoteIdentification() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemoteProtocolVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getChannelCount() {
		// TODO Auto-generated method stub
		return 0;
	}

//	private void doRead(SelectionKey selectionKey) throws IOException {
//		ByteBuffer byteBuffer = ByteBuffer.allocate(1024); // pos=0 & lim=1024
//		int read = socketChannel.read(byteBuffer); // pos=numberOfBytes & lim=1024
//		if (read == -1) { // if connection is closed by the client
//			close();
//		} else {
//			byteBuffer.flip(); // put buffer in read mode by setting pos=0 and lim=numberOfBytes
//			StringBuilder message = new StringBuilder();
//			while (byteBuffer.hasRemaining()) {
//				message.append((char) byteBuffer.get());
//			}
//			if ("bye".equals(message.toString().trim())) {
//				byteBuffer.rewind();
//				createResponse(socketChannel, byteBuffer);
//				close();
//			} else {
//				byteBuffer.rewind();
//				pendingData.add(byteBuffer); // find socket channel and add new byteBuffer queue
//				selectionKey.interestOps(SelectionKey.OP_WRITE); // set mode to WRITE to send data
//			}
//		}
//	}
//
//	private void doWrite(SelectionKey selectionKey) {
//		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//		while (!pendingData.isEmpty()) { // start sending to client from queue
//			ByteBuffer buf = pendingData.poll();
//			createResponse(socketChannel, buf);
//		}
//		selectionKey.interestOps(SelectionKey.OP_READ); // change the key to READ
//	}
//
//	private void createResponse(SocketChannel socketChannel, ByteBuffer buf) {
//		try {
//			buf.rewind();
//			ByteBuffer response = ByteBuffer.allocate(1024)
//					.put(("[" + socketChannel.getRemoteAddress() + "]: ").getBytes()).put(buf);
//			response.rewind();
//			socketChannel.write(response);
//			response.clear();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	public class ClientHandler extends AbstractStreamHandler {

		@Override
		public void read(Object msg) {
			System.err.println(new String((byte[]) msg));
			// Some code
		}

		@Override
		public void event(SessionEvent event) {
			switch (event) {
			case READY:
				getSession().write("Hello, World!".getBytes());
				getSession().close();
				break;
			}
		}

		@Override
		public ISessionConfig getConfig() {
			return new DefaultSessionConfig().setEndingAction(EndingAction.STOP);
		}
	}

	@Override
	public void addListener(SshClientListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(SshClientListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<SshLifecycleComponent<?, ?>> getAllActiveComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends SshLifecycleComponent<?, ?>> Set<T> getActiveComponents(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connect(String username, String hostname, int port, SshAuthenticator... authenticators)
			throws SshException {
		// TODO make this default implmentation? would conflict with the reverse currently being default, i.e. simulated non-blocking IO
		try {
			connectLater(username, hostname, port, authenticators).get(getConfiguration().getIoTimeout(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new SshException(SshException.IO_ERROR, e);
		}
	}

	@Override
	public Future<Void> connectLater(String username, String hostname, int port, SshAuthenticator... authenticators) {
		var address = new InetSocketAddress(hostname, port);
		try {
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.connect(address);
			
			//Create the engine session
			StreamSession session = new EngineSession(EngineFactory.create(2, 0, true), new EngineClientHandler());
			
			// Register the channel
			getNasshProvider().getLoop().register(socketChannel, session);
			
			// Confirm that the connection was successful
			session.getReadyFuture().sync();

			session.write("Hello, World!".getBytes()).sync();
			
			session.quickClose();

			session.getCloseFuture().sync();
			
			
			
			return getNasshProvider().getLoop().register(socketChannel, new ClientHandler());
		} catch (Exception e) {
			return new FailedFuture<>(null, new SshException(SshException.IO_ERROR, "Failed to connect.", e));
		}
	}

	@Override
	public Future<Boolean> authenticateLater(SshAuthenticator... authenticators) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshShell createShell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshShell shell(String termType, int cols, int rows, int pixWidth, int pixHeight, byte[] terminalModes)
			throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<SshShell> shellLater(String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPortForward createLocalForward(String localBindAddress, int localBindPort, String targetAddress,
			int targetPort) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPortForward localForward(String localBindAddress, int localBindPort, String targetAddress, int targetPort)
			throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPortForward createRemoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPortForward remoteForward(String remoteBindAddress, int remoteBindPort, String targetAddress,
			int targetPort) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshCommand command(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshCommand createCommand(String command, String termType, int cols, int rows, int pixWidth, int pixHeight,
			byte[] terminalModes) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SocketFactory createTunneledSocketFactory() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshSCPClient createSCP() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SftpClient createSftp() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SftpClient sftp() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshCustomChannel createForwardingChannel(String hostname, int port) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshCustomChannel forwardingChannel(String hostname, int port) throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPublicKeySubsystem createPublicKeySubsystem() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SshPublicKeySubsystem publicKeySubsystem() throws SshException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHostname() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPortForwardListener(SshPortForwardListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePortForwardListener(SshPortForwardListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(int timeout) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTimeout() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addChannelHandler(SshChannelHandler channelHandler) throws SshException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeChannelHandler(SshChannelHandler channelHandler) throws SshException {
		// TODO Auto-generated method stub

	}

	@Override
	public void forceKeyExchange() throws SshException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	private NaSshProvider getNasshProvider() {
		return (NaSshProvider)getProvider();
	}
}
