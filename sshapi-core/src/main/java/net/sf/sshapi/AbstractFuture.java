package net.sf.sshapi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractFuture<V> implements Future<V> {
	private boolean cancelled;
	private V val;
	private Exception exception;
	private Thread thread;
	private boolean done;
	private Semaphore sem = new Semaphore(1);
	
	AbstractFuture() {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	Runnable createRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					val = doFuture();
				} catch (Exception e) {
					exception = e;
				} finally {
					sem.release();
					done = true;
				}
			}
		};
	}
	
	abstract V doFuture() throws Exception;
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (done || cancelled)
			return false;
		else {
			cancelled = true;
			if(thread != null && mayInterruptIfRunning)
				thread.interrupt();
			return true;
		}
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		sem.acquire();
		try {
			if(exception != null)
				throw new ExecutionException(exception);
			return val;
		} finally {
			sem.release();
		}
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (sem.tryAcquire(timeout, unit))
			sem.release();
		if(exception != null)
			throw new ExecutionException(exception);
		return val;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}
}
