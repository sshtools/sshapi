package com.maverick.echo;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class EchoClient {

	ClientBootstrap clientBootstrap;
	private String host;
	private int port;
	private int concurrentClients;
	private int minExchanges;
	private int maxExchanges;
	private int minBlockSize;
	private int maxBlockSize;
	private SecureRandom rnd = new SecureRandom();
	private boolean exitOnError = false;
	private int errors;
	private int total;
	private int running;
	private boolean successfulConnectionCompleted = false;
	private int connectionFailures;
	long started;
	long transferred;

	Object lock = new Object();

	public EchoClient(String host, int port, int concurrentClients,
			int minExchanges, int maxExchanges, int minBlockSize,
			int maxBlockSize) {

		this.host = host;
		this.port = port;
		this.concurrentClients = concurrentClients;
		this.minExchanges = minExchanges;
		this.maxExchanges = maxExchanges;
		this.minBlockSize = minBlockSize;
		this.maxBlockSize = maxBlockSize;

		clientBootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));

		clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("handler", new EchoClientHandler(
						EchoClient.this, getNumberOfExchanges()));
				return pipeline;
			}
		});

		rnd.setSeed(port * concurrentClients * minExchanges * maxExchanges
				* minBlockSize * maxBlockSize);
		rnd.setSeed(System.currentTimeMillis());
	}

	public void setExitOnError(boolean exitOnError) {
		this.exitOnError = exitOnError;
	}

	public boolean isExitOnError() {
		return exitOnError;
	}

	private int getNumberOfExchanges() {
		if (maxExchanges > minExchanges) {
			return rnd.nextInt(maxExchanges - minExchanges) + minExchanges;
		} else {
			return maxExchanges;
		}
	}

	public int getMinBlockSize() {
		return minBlockSize;
	}

	public int getMaxBlockSize() {
		return maxBlockSize;
	}

	public Random getRnd() {
		return rnd;
	}

	public int getTotal() {
		return total;
	}

	public synchronized void registerError() {
		errors++;
	}

	public int getErrors() {
		return successfulConnectionCompleted ? errors : connectionFailures;
	}

	public long getStarted() {
		return started;
	}

	public synchronized void logTransfer(long bytes) {
		transferred += bytes;
	}

	public long getBytesTransfered() {
		return transferred;
	}

	private void connect(ChannelFutureListener onConnect,
			ChannelFutureListener onClose) {
		ChannelFuture future = clientBootstrap.connect(new InetSocketAddress(
				host, port));
		future.addListener(onConnect);
		future.getChannel().getCloseFuture().addListener(onClose);
	}

	private void connect(final Date until) {

		final ChannelFutureListener onConnect = new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future)
					throws Exception {
				synchronized (lock) {

					if (future.isSuccess()) {
						if (!successfulConnectionCompleted) {
							System.out
									.println("Successfull connection completed");
						}
						successfulConnectionCompleted = true;
						total++;
						running++;
					} else {
						if (successfulConnectionCompleted) {
							System.out
									.println("Connect error after successfull connection completed cancelled="
											+ future.isCancelled()
											+ " done="
											+ future.isDone());
							registerError();
						} else {
							connectionFailures++;
						}
						running--;

						if (until.after(new Date())) {
							connect(until);
						} else {
							lock.notify();
						}
					}
				}
			}
		};

		ChannelFutureListener onClose = new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future)
					throws Exception {
				synchronized (lock) {
					running--;

					if (until.after(new Date())) {
						connect(until);
					} else {
						lock.notify();
					}
				}

			}
		};

		connect(onConnect, onClose);
	}

	public void runUntil(final Date date) {

		started = System.currentTimeMillis();

		synchronized (lock) {
			for (int i = 0; i < concurrentClients; i++) {
				connect(date);
			}

			while (date.after(new Date()) || running > 0) {
				try {
					lock.wait(500);
				} catch (InterruptedException e) {
				}
			}

		}
	}

	/**
	 * <p>
	 * Run once using the supplied configuration. This method will keep
	 * attempting connections until at least one can be made, at that point it
	 * will allow all other connections to start and the test to continue.
	 * 
	 * @param timeout
	 *            if no connections are made before this timeout, run will exit.
	 *            Set to zero to exit immediately.
	 */
	public void run(long timeout) {

		started = System.currentTimeMillis();
		final long giveUpTime = timeout == 0 ? 0 : (started + timeout);

		synchronized (lock) {

			try {
				for (int i = 0; i < concurrentClients; i++) {
					final long connecting = System.currentTimeMillis();
					final ChannelFutureListener onClose = new ChannelFutureListener() {

						public void operationComplete(ChannelFuture future)
								throws Exception {
							synchronized (lock) {
								running--;
								lock.notify();
							}
						}
					};
					ChannelFutureListener onConnect = new ChannelFutureListener() {

						public void operationComplete(ChannelFuture future)
								throws Exception {
							synchronized (lock) {
								System.out
										.println("Connect time "
												+ (System.currentTimeMillis() - connecting)
												+ " ms");
								running++;
								if (future.isSuccess()) {
									if (!successfulConnectionCompleted) {
										System.out
												.println("Successfull connection completed");
									}
									successfulConnectionCompleted = true;
									total++;
									lock.notifyAll();
								} else {
									if (successfulConnectionCompleted) {
										System.out
												.println("Connect error after successfull connection completed cancelled="
														+ future.isCancelled()
														+ " done="
														+ future.isDone());
										registerError();
									} else {
										connectionFailures++;
									}
									if (isGivenUp(giveUpTime)) {
										lock.notifyAll();
									} else {
										connect(this, onClose);
									}
								}

							}
						}
					};
					connect(onConnect, onClose);
					if (i == 0) {
						// Wait for the first connection to complete before
						// allowing more, this mitigates too many open files
						// error
						while (!successfulConnectionCompleted
								&& !isGivenUp(giveUpTime)) {
							lock.wait(500);
						}
					}
				}

				while (running > 0
						&& (successfulConnectionCompleted || !isGivenUp(giveUpTime))) {
					lock.wait(500);
				}
			} catch (InterruptedException e) {
			}

		}

	}

	private boolean isGivenUp(final long giveUpTime) {
		return giveUpTime == 0 || System.currentTimeMillis() > giveUpTime;
	}

	public String getStatistics() {

		long bytes = transferred % 1024;
		long kb = transferred >= 1024 ? transferred / 1024 : 0;
		long mb = kb >= 1024 ? kb / 1024 : 0;

		if (kb > 0)
			kb = kb % 1024;

		long gb = mb >= 1024 ? mb / 1024 : 0;

		if (mb > 0)
			mb = mb % 1024;

		StringBuffer bf = new StringBuffer();

		if (gb > 0) {
			bf.append(gb);
			bf.append("GB");
		}
		if (mb > 0) {
			bf.append(" ");
			bf.append(mb);
			bf.append("MB");
		}
		if (kb > 0) {
			bf.append(" ");
			bf.append(kb);
			bf.append("KB");
		}
		if (bytes > 0) {
			bf.append(" ");
			bf.append(bytes);
			bf.append("B");
		}
		return bf.toString().trim();

	}

}
