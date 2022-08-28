package net.sf.sshapi.examples;
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
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates how you can bypass the automatic selection of a
 * provider and list and choose the SSH implementation to use.
 */
public class E99ChoosingAProvider {
	
	static {
		System.setProperty("sshapi.logLevel", "DEBUG");
	}
	
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		// List all of the providers and allow the user to select one
		SshProvider[] providers = DefaultProviderFactory.getAllProviders();
		System.out.println("Providers :-");
		for (int i = 0; i < providers.length; i++) {
			System.out.println(String.format("  %2d : %-20s (%s %s)", i + 1, providers[i].getName(),
					providers[i].getVersion(), providers[i].getVendor()));
		}
		SshProvider provider = providers[Integer.parseInt(Util.prompt("\nEnter the number for the provider you wish to use (1-"
			+ providers.length + ")")) - 1];

		/**
		 * Set the
		 * system property to use a specific provider 
		 */
		System.setProperty(DefaultProviderFactory.PROVIDER_CLASS_NAME, provider.getClass().getName());
		  
		String[] tests = new String[] { "E01Shell", "E02ShellWithConsolePrompts", "E03ShellWithGUIPrompts",
					"E04ExecuteCommand", "E05X11Forwarding", "E06bLocalForwardingAndShell", "E06LocalForwarding",
					"E07RemoteForwarding", "E08Sftp", "E09SSH1Only", "E10PublicKeyAuthentication", "E11KeyboardInteractiveAuthentication",
					"E12ChangeKeyPassphrase", "E13ExtendedHostKeyValidation", "E14HostKeyManagement",
					"E15SCP", "E16PublicKeySubsystem", "E17TunneledSocketFactory", "E19ShellUsingGSSAPI",
					"E20CustomChannel", "E21AgentAuthentication", "E22RawSFTP", "E23NonBlockingConsole", "E24NonBlockingConsoleTasks",
					"E25ForwardingChannel" };
		System.out.println();
		for(int i = 0 ; i < tests.length ; i++)
			System.out.println((i+ 1) + ". " + tests[i]);

		String test = tests[Integer.parseInt(Util.prompt("\n\nEnter the test number (1-"
			+ tests.length + ")")) - 1];
		
		Class.forName(E99ChoosingAProvider.class.getPackageName() + "." + test).getMethod("main", String[].class).invoke(null, (Object)arg);
	}
}
