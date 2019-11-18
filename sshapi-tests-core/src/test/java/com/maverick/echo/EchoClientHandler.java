package com.maverick.echo;

import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoClientHandler extends SimpleChannelUpstreamHandler {

	private static Logger log = LoggerFactory.getLogger(EchoClient.class);
	
	EchoClient client;
	int requestsOutstanding;
	byte[] lastMessage;
	Channel ch;
	long bytesTransferred;
	
	public EchoClientHandler(EchoClient client, int requestsOutstanding) {
		this.client = client;
		this.requestsOutstanding = requestsOutstanding;
	}
	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		
		if(requestsOutstanding > 0) {
			if(log.isDebugEnabled()) {
				log.debug("exceptionCaught " + e.getChannel().getLocalAddress() + " requests=" + requestsOutstanding + " total=" + client.getTotal(), e.getCause());
				if(client.isExitOnError())
					System.exit(0);
			}
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if(log.isTraceEnabled()) {
			log.debug("Client connected " + e.getChannel().getLocalAddress() + " requests=" + requestsOutstanding);
		}
		ch = e.getChannel();
		echoMessage(ctx);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		
		if(log.isTraceEnabled()) {
			log.debug("Client disconnected " + e.getChannel().getLocalAddress() + " requests=" + requestsOutstanding);
		}
		if(requestsOutstanding > 0)
			client.registerError();
		
		client.logTransfer(bytesTransferred);

	}
	
	private void echoMessage(ChannelHandlerContext ctx) {
		
		if(requestsOutstanding > 0) {
			ctx.setAttachment(null);
			lastMessage = new byte[getBlockSize()];
			client.getRnd().nextBytes(lastMessage);
			ChannelBuffer echoMessage = ChannelBuffers.copiedBuffer(lastMessage);
			
			if(log.isTraceEnabled()) {
				log.debug("Sending message of " + lastMessage.length + " bytes to server from " + ch.getLocalAddress());
			}
			ch.write(echoMessage);
			
		} else {
			ch.close();
		}
	}
	
	private int getBlockSize() {
		if(client.getMaxBlockSize() > client.getMinBlockSize()) {
			return client.getRnd().nextInt(client.getMaxBlockSize()-client.getMinBlockSize())+client.getMinBlockSize();
		} else {
			return client.getMaxBlockSize();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
		if(ctx.getAttachment()==null) {
			ctx.setAttachment(ChannelBuffers.buffer(lastMessage.length));
		}
		ChannelBuffer buf2 = (ChannelBuffer) ctx.getAttachment();
		
		if(log.isTraceEnabled()) {
			log.debug("Received message of " + buf.readableBytes() + " bytes on " + ch.getLocalAddress());
		}
		
		buf.readBytes(buf2, Math.min(buf2.capacity(), buf.readableBytes()));
		
		if(buf2.readableBytes() == lastMessage.length) {
			if(!Arrays.equals(buf2.array(), lastMessage)) {
				if(log.isDebugEnabled()) {
					log.debug("DATA ERROR: message received from server does not have the expected content!!");
				}
				ch.close();
			} else {
				requestsOutstanding--;
				bytesTransferred += lastMessage.length;
				echoMessage(ctx);
			}
		}
	}

}
