/* 
 * Copyright (c) 2010 The JavaSSH Project
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

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import net.sf.sshapi.SshClient;
import net.sf.sshapi.auth.SshGSSAPIAuthenticator;

/**
 * Default implementation of an {@link SshGSSAPIAuthenticator}. This should
 * suffice for most uses.
 * 
 * See the documentation for
 * {@link com.sun.security.auth.module.Krb5LoginModule} for more details.
 */
@SuppressWarnings("restriction")
public class DefaultGSSAuthenticator implements SshGSSAPIAuthenticator {

	private String principal;
	private boolean debug;
	private boolean doNotPrompt = true;
	private boolean storeKey;
	private boolean tryFirstPass = true;
	private boolean useTicketCache = true;
	private boolean useKeyTab = true;

	/**
	 * Constructor.
	 * 
	 * @param principal
	 */
	public DefaultGSSAuthenticator(String principal) {
		this.principal = principal;
	}

	/**
	 * Set the name of the principal that should be used. It could be simple
	 * username such as "testuser" or a service name such as
	 * "host/testhost.eng.sun.com" . You can use principal option to set the
	 * principal when there are credentials for multiple principals in the
	 * keyTab or when you want a specific ticket cache only.
	 * 
	 * @param principal principal
	 */
	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	/**
	 * Get the name of the principal that should be used. It could be simple
	 * username such as "testuser" or a service name such as
	 * "host/testhost.eng.sun.com" . You can use principal option to set the
	 * principal when there are credentials for multiple principals in the
	 * keyTab or when you want a specific ticket cache only.
	 * 
	 * @return principal
	 */
	public String getPrincipal() {
		return principal;
	}

	/**
	 * This is true if you want the module to get the principal's key from the
	 * the keytab.(default value is False) If keyatb is not set then the module
	 * will locate the keytab from the Kerberos configuration file. If it is not
	 * specifed in the Kerberos configuration file then it will look for the
	 * file {user.home}{file.separator}krb5.keytab.
	 * 
	 * @return use key tab.
	 * @see #setUseKeyTab(boolean)
	 */
	public boolean isUseKeyTab() {
		return useKeyTab;
	}

	/**
	 * Set this to true if you want the module to get the principal's key from
	 * the the keytab.(default value is False) If keyatb is not set then the
	 * module will locate the keytab from the Kerberos configuration file. If it
	 * is not specifed in the Kerberos configuration file then it will look for
	 * the file {user.home}{file.separator}krb5.keytab.
	 * 
	 * @param useKeyTab use key tab.
	 */
	public void setUseKeyTab(boolean useKeyTab) {
		this.useKeyTab = useKeyTab;
	}

	/**
	 * Get whether GSSAPI operations should output debug.
	 * 
	 * @return debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Set whether GSSAPI operations should output debug.
	 * 
	 * @param debug debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * True if you do not want to be prompted for the password if credentials
	 * can not be obtained from the cache or keytab.(Default is false) If set to
	 * true authentication will fail if credentials can not be obtained from the
	 * cache or keytab.
	 * 
	 * @return do not prompt
	 */
	public boolean isDoNotPrompt() {
		return doNotPrompt;
	}

	/**
	 * Set this to true if you do not want to be prompted for the password if
	 * credentials can not be obtained from the cache or keytab.(Default is
	 * false) If set to true authentication will fail if credentials can not be
	 * obtained from the cache or keytab.
	 * 
	 * @param doNotPrompt do not prompt
	 */
	public void setDoNotPrompt(boolean doNotPrompt) {
		this.doNotPrompt = doNotPrompt;
	}

	/**
	 * Get if you want the principal's key to be stored in the Subject's private
	 * credentials.
	 * 
	 * @return store principal's key in subjects private credentials
	 */
	public boolean isStoreKey() {
		return storeKey;
	}

	/**
	 * Set this to True to if you want the principal's key to be stored in the
	 * Subject's private credentials.
	 * 
	 * @param storeKey store principal's key in subjects private credentials
	 */
	public void setStoreKey(boolean storeKey) {
		this.storeKey = storeKey;
	}

	/**
	 * Get if the username and password should be retrieved from the module's
	 * shared state using "javax.security.auth.login.name" and
	 * "javax.security.auth.login.password" as the respective keys. The
	 * retrieved values are used for authentication. If authentication fails,
	 * the module uses the CallbackHandler to retrieve a new username and
	 * password, and another attempt to authenticate is made. If the
	 * authentication fails, the failure is reported back to the calling
	 * application
	 * 
	 * @return try first pass
	 */
	public boolean isTryFirstPass() {
		return tryFirstPass;
	}

	/**
	 * Set to true to retrieve the the username and password from the module's
	 * shared state using "javax.security.auth.login.name" and
	 * "javax.security.auth.login.password" as the respective keys. The
	 * retrieved values are used for authentication. If authentication fails,
	 * the module uses the CallbackHandler to retrieve a new username and
	 * password, and another attempt to authenticate is made. If the
	 * authentication fails, the failure is reported back to the calling
	 * application
	 * 
	 * @param tryFirstPass try first pass
	 */
	public void setTryFirstPass(boolean tryFirstPass) {
		this.tryFirstPass = tryFirstPass;
	}

	/**
	 * This is true if you want the TGT to be obtained from the ticket cache.
	 * This is false if you do not want this module to use the ticket cache.
	 * (Default is False). This module will search for the ticket cache in the
	 * following locations: For Windows 2000, it will use Local Security
	 * Authority (LSA) API to get the TGT. On Solaris and Linux it will look for
	 * the ticket cache in /tmp/krb5cc_uid where the uid is numeric user
	 * identifier. If the ticket cache is not available in either of the above
	 * locations, or if we are on a different WIndows platform, it will look for
	 * the cache as {user.home}{file.separator}krb5cc_{user.name}. You can
	 * override the ticket cache location by using ticketCache
	 * 
	 * @return use ticket cache
	 */
	public boolean isUseTicketCache() {
		return useTicketCache;
	}

	/**
	 * Set this to true, if you want the TGT to be obtained from the ticket
	 * cache. Set this option to false if you do not want this module to use the
	 * ticket cache. (Default is False). This module will search for the tickect
	 * cache in the following locations: For Windows 2000, it will use Local
	 * Security Authority (LSA) API to get the TGT. On Solaris and Linux it will
	 * look for the ticket cache in /tmp/krb5cc_uid where the uid is numeric
	 * user identifier. If the ticket cache is not available in either of the
	 * above locations, or if we are on a different WIndows platform, it will
	 * look for the cache as {user.home}{file.separator}krb5cc_{user.name}. You
	 * can override the ticket cache location by using ticketCache
	 * 
	 * @param useTicketCache use ticket cache
	 */
	public void setUseTicketCache(boolean useTicketCache) {
		this.useTicketCache = useTicketCache;
	}

	public Configuration getConfiguration() {
		return new Configuration() {
			public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
				Map<String, String> options = new HashMap<>();
				if (principal != null) {
					options.put("principal", principal);
				}
				options.put("debug", String.valueOf(debug));
				options.put("storeKey", String.valueOf(storeKey));
				options.put("useKeyTab", String.valueOf(useKeyTab));
				options.put("useTicketCache", String.valueOf(useTicketCache));
				options.put("tryFirstPass", String.valueOf(tryFirstPass));
				options.put("doNotPrompt", String.valueOf(doNotPrompt));
				return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
					LoginModuleControlFlag.REQUIRED, options) };
			}
		};
	}

	public char[] promptForPassword(SshClient session, String message) {
		return null;
	}

}
