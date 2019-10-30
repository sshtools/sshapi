package net.sf.sshapi;

/**
 * Events fired by {@link SshClient} instances.
 */
public interface SshClientListener {
	/**
	 * A new component has been created.
	 * 
	 * @param component component created
	 */
	void created(SshLifecycleComponent<?, ?> component);

	/**
	 * An existing component has been removed.
	 * 
	 * @param component component created
	 */
	void removed(SshLifecycleComponent<?, ?> component);
}
