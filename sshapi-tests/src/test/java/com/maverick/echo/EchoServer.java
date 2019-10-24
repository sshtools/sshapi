package com.maverick.echo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


public class EchoServer {

	ServerBootstrap serverBootstrap;
	int port;
	private Channel channel;
	
	public EchoServer(int port) {
		this.port = port;
		
		serverBootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new EchoServerHandler());
				return pipeline;
			}
		});
		
		
	}
	
	public void start() {
		channel = serverBootstrap.bind(new InetSocketAddress("127.0.0.1", port));
	}
	
	public int getListeningPort() {
		return ((InetSocketAddress)channel.getLocalAddress()).getPort();
	}
	
	public InetAddress getListeningAddress() {
		return ((InetSocketAddress)channel.getLocalAddress()).getAddress();
	}

	public void stop() throws InterruptedException {
		channel.close().await();
		
	}
	
	public static void main(String[] args) {
		
		PropertyConfigurator.configure(EchoServer.class.getResource("/log4j.properties"));
		
		EchoServer server = new EchoServer(9001);
		
		server.start();
	}

}
