package com.maverick.echo;

import java.util.Calendar;

import org.apache.log4j.PropertyConfigurator;

public class LoadTestClient {

	public static void main(String[] args) {
		
		PropertyConfigurator.configure(LoadTestClient.class.getResource("/log4j.properties"));
		
		if(args.length!=8) {
			System.out.println("Usage: <minutes> <host> <port> <numClients> <minPackets> <maxPackets> <minPacketLength> <maxPacketLength>");
		}
		
		EchoClient client;
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, Integer.parseInt(args[0]));
		client = new EchoClient(args[1], 
					Integer.parseInt(args[2]), 
					Integer.parseInt(args[3]), 
					Integer.parseInt(args[4]), 
					Integer.parseInt(args[5]), 
					Integer.parseInt(args[6]), 
					Integer.parseInt(args[7]));
		
		client.runUntil(c.getTime());	
		
		System.out.println(client.getStatistics() + " transferred in " + (System.currentTimeMillis() - client.getStarted()) + "ms with " + client.getErrors() + " errors");
		
		System.exit(0);
	}
}
