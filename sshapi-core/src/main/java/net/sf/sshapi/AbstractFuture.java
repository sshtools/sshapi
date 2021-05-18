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
package net.sf.sshapi;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Class AbstractFuture.
 *
 * @param <V> the value type
 */
public abstract class AbstractFuture<V> implements Future<V> {
	
	/** The cancelled. */
	private boolean cancelled;
	
	/** The val. */
	private V val;
	
	/** The exception. */
	private Exception exception;
	
	/** The thread. */
	private Thread thread;
	
	/** The done. */
	private boolean done;
	
	/** The sem. */
	private Semaphore sem = new Semaphore(1);
	
	/**
	 * Instantiates a new abstract future.
	 */
	AbstractFuture() {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Creates the runnable.
	 *
	 * @return the runnable
	 */
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
	
	/**
	 * Do future.
	 *
	 * @return the v
	 * @throws Exception the exception
	 */
	abstract V doFuture() throws Exception;
	
	/**
	 * Cancel.
	 *
	 * @param mayInterruptIfRunning the may interrupt if running
	 * @return true, if successful
	 */
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

	/**
	 * Gets the.
	 *
	 * @return the v
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException the execution exception
	 */
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

	/**
	 * Gets the.
	 *
	 * @param timeout the timeout
	 * @param unit the unit
	 * @return the v
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException the execution exception
	 * @throws TimeoutException the timeout exception
	 */
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (sem.tryAcquire(timeout, unit))
			sem.release();
		if(exception != null)
			throw new ExecutionException(exception);
		return val;
	}

	/**
	 * Checks if is cancelled.
	 *
	 * @return true, if is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Checks if is done.
	 *
	 * @return true, if is done
	 */
	@Override
	public boolean isDone() {
		return done;
	}
}
