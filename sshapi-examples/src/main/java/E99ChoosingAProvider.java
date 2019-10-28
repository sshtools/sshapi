
import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.Logger.Level;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.ConsoleLogger;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates how you can bypass the automatic selection of a
 * provider and list and choose the SSH implementation to use.
 */
public class E99ChoosingAProvider {
	static {
		((ConsoleLogger)SshConfiguration.getLogger()).setDefaultLevel(Level.INFO);
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
			System.out.println("  " + (i + 1) + ": " + providers[i].getClass().getName());
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
					"E20CustomChannel", "E21AgentAuthentication", "E22RawSFTP", "E23NonBlockingConsole", "E24NonBlockingConsoleTasks" };
		System.out.println();
		for(int i = 0 ; i < tests.length ; i++)
			System.out.println((i+ 1) + ". " + tests[i]);

		String test = tests[Integer.parseInt(Util.prompt("\n\nEnter the test number (1-"
			+ tests.length + ")")) - 1];
		
		Class.forName(test).getMethod("main", String[].class).invoke(null, (Object)arg);
	}
}
