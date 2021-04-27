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
