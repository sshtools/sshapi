package net.sf.sshapi.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import net.sf.sshapi.SshClient;

public class RemoteSocketFactory extends SocketFactory {
	private SshClient client;

	public RemoteSocketFactory(SshClient client) {
		this.client = client;
	}

	@Override
	public Socket createSocket() throws IOException {
		return new RemoteSocket(client);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return new RemoteSocket(client, host.getHostAddress(), port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return new RemoteSocket(client, address.getHostAddress(), port);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return new RemoteSocket(client, host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		return new RemoteSocket(client, host, port);
	}
}