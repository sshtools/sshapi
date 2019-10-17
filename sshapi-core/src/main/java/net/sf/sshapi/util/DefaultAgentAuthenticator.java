/* 
 * Copyright (c) 2018 The JavaSSH Project
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.sf.sshapi.util;

import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshException;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.agent.SshAgent;
import net.sf.sshapi.auth.SshAgentAuthenticator;

/**
 * Default implementation of an {@link SshAgentAuthenticator}. 
 */
public class DefaultAgentAuthenticator implements SshAgentAuthenticator {

	private SshAgent agent;

	/**
	 * Constructor. For this usage, {@link SshConfiguration#setAgent(SshAgent)} 
	 * should have been used to set the agent to use.
	 */
	public DefaultAgentAuthenticator() {
	}

	/**
	 * Constructor.
	 * 
	 * @param agent agent
	 */
	public DefaultAgentAuthenticator(SshAgent agent) {
		this.agent = agent;
	}

	public String getTypeName() {
		return "agent";
	}
	
	public SshAgent getAgent(SshConfiguration configuration) {
		@SuppressWarnings("resource")
		SshAgent a = agent == null ? configuration.getAgent() : agent;
		if(a == null) {
			// Locate and connect to agent, and set it on the configuration
			SshProvider provider = DefaultProviderFactory.getInstance().getProvider(configuration);
			try {
				a = provider.connectToLocalAgent("SSHAPI");
			} catch (SshException e) {
				throw new IllegalStateException("Could not connect to local agent.", e);
			}
		}
		return a;
	}
}
