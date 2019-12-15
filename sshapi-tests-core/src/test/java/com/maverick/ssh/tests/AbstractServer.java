package com.maverick.ssh.tests;

import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.Logger;
import net.sf.sshapi.SshConfiguration;

public abstract class AbstractServer implements ServerService {
	private static final Logger LOG = SshConfiguration.getLogger();
	protected List<AuthenticationMethod> methods = new ArrayList<AuthenticationMethod>();
	private boolean started;

	public void addRequiredAuthentication(AuthenticationMethod method) {
		methods.add(method);
	}

	public void removeRequiredAuthentication(AuthenticationMethod method) {
		methods.remove(method);
	}

	public final void start() throws Exception {
		LOG.info("Starting {0}", getClass().getSimpleName());
		doStart();
		started = true;
		LOG.info("Started {0}", getClass().getSimpleName());
	}

	protected abstract void doStart() throws Exception;

	public final void stop() throws Exception {
		LOG.info("Stopping {0}", getClass().getSimpleName());
		try {
			doStop();
		} finally {
			started = false;
			LOG.info("Stopped {0}", getClass().getSimpleName());
		}
	}

	protected boolean isStarted() {
		return started;
	}

	protected abstract void doStop() throws Exception;

	public void restart() throws Exception {
		if (started) {
			stop();
		}
		start();
	}
}
