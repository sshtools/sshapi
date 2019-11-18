package com.maverick.echo;

import org.apache.log4j.BasicConfigurator;

public class TimedClient {

	
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		
		if(args.length!=8) {
			System.out.println("Usage: <delay> <host> <port> <numCycles> <minPackets> <maxPackets> <minPacketLength> <maxPacketLength>");
		}
		
		EchoClient client;
		int delay = Integer.parseInt(args[0]);
		int numCycles = Integer.parseInt(args[3]);
		
		client = new EchoClient(args[1], 
					Integer.parseInt(args[2]), 
					1, 
					Integer.parseInt(args[4]), 
					Integer.parseInt(args[5]), 
					Integer.parseInt(args[6]), 
					Integer.parseInt(args[7]));
		
		for(int i=0;i<numCycles;i++) {
			System.out.println("Starting cycle " + i);
			long start = System.currentTimeMillis();
			client.run(0);
			System.out.println("Completed transfer in " + (System.currentTimeMillis()-start) + " ms");
			try {
				Thread.sleep(delay*1000*60);
			} catch (InterruptedException e) {
			}
			System.out.println();
		}
		
		System.exit(0);
	}
}
