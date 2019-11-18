package com.maverick.echo;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoServerHandler extends SimpleChannelUpstreamHandler{

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent evt)
			throws Exception {
		if(log.isDebugEnabled()) {
			log.debug("Exception caught from " + evt.getChannel().getRemoteAddress(), evt.getCause());
		}
	}

	private static Logger log = LoggerFactory.getLogger(EchoServerHandler.class);
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		if(log.isDebugEnabled()) {
			log.debug("Client connected from " + e.getChannel().getRemoteAddress());
		}
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug("Client disconnected from " + e.getChannel().getRemoteAddress());
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		
		ChannelBuffer buf = (ChannelBuffer)e.getMessage();
		ChannelBuffer buf2 = buf.copy();
		e.getChannel().write(buf2);
	}

}
