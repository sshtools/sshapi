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
