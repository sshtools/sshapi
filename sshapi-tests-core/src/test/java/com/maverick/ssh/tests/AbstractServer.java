package com.maverick.ssh.tests;

import java.util.ArrayList;
import java.util.List;

import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.Logger.Level;

public abstract class AbstractServer implements ServerService {
	protected List<AuthenticationMethod> methods = new ArrayList<AuthenticationMethod>();
	private boolean started;

	public void addRequiredAuthentication(AuthenticationMethod method) {
		methods.add(method);
	}

	public void removeRequiredAuthentication(AuthenticationMethod method) {
		methods.remove(method);
	}

	public final void start() throws Exception {
		SshConfiguration.getLogger().log(Level.INFO, "Starting " + getClass().getSimpleName());
		doStart();
		started = true;
		SshConfiguration.getLogger().log(Level.INFO, "Started " + getClass().getSimpleName());
	}

	protected abstract void doStart() throws Exception;

	public final void stop() throws Exception {
		SshConfiguration.getLogger().log(Level.INFO, "Stopping " + getClass().getSimpleName());
		try {
			doStop();
		} finally {
			started = false;
			SshConfiguration.getLogger().log(Level.INFO, "Stopped " + getClass().getSimpleName());
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
