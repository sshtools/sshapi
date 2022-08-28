package net.sf.sshapi.impl.nassh;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class NaSshSelectorPool {
	protected volatile Selector sharedSelector;
	protected int maxSelectors = Runtime.getRuntime().availableProcessors();
	protected int maxSpareSelectors = -1;
	protected boolean enabled = true;
	protected AtomicInteger active = new AtomicInteger(0);
	protected AtomicInteger spare = new AtomicInteger(0);
	protected ConcurrentLinkedQueue<Selector> selectors = new ConcurrentLinkedQueue<>();

	public Selector get() throws IOException {
		if ((!enabled) || active.incrementAndGet() >= maxSelectors) {
			if (enabled) {
				active.decrementAndGet();
			}
			return null;
		}
		Selector s = null;
		try {
			s = selectors.size() > 0 ? selectors.poll() : null;
			if (s == null) {
				s = Selector.open();
			} else {
				spare.decrementAndGet();
			}
		} catch (NoSuchElementException x) {
			try {
				s = Selector.open();
			} catch (IOException iox) {
			}
		} finally {
			if (s == null) {
				active.decrementAndGet();// we were unable to find a selector
			}
		}
		return s;
	}

	public void put(Selector s) throws IOException {
		if (enabled) {
			active.decrementAndGet();
		}
		if (enabled && (maxSpareSelectors == -1 || spare.get() < Math.min(maxSpareSelectors, maxSelectors))) {
			spare.incrementAndGet();
			selectors.offer(s);
		} else {
			s.close();
		}
	}

	public void close() throws IOException {
		enabled = false;
		Selector s;
		while ((s = selectors.poll()) != null) {
			s.close();
		}
		spare.set(0);
		active.set(0);
	}

	public void open(String name) throws IOException {
		enabled = true;
	}

	public void setMaxSelectors(int maxSelectors) {
		this.maxSelectors = maxSelectors;
	}

	public void setMaxSpareSelectors(int maxSpareSelectors) {
		this.maxSpareSelectors = maxSpareSelectors;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getMaxSelectors() {
		return maxSelectors;
	}

	public int getMaxSpareSelectors() {
		return maxSpareSelectors;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ConcurrentLinkedQueue<Selector> getSelectors() {
		return selectors;
	}

	public AtomicInteger getSpare() {
		return spare;
	}
}