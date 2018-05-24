import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshProvider;
import net.sf.sshapi.util.Util;

/**
 * This example demonstrates how you can bypass the automatic selection of a
 * provider and list and choose the SSH implementation to use.
 */
public class E99ChoosingAProvider {
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
		 * Now re-use the {@link ShellWithConsolePrompts} example. Set the
		 * system property to use a specific provider and run it
		 */
		System.setProperty(DefaultProviderFactory.PROVIDER_CLASS_NAME, provider.getClass().getName());
//		  E01Shell.main(arg);
//		 E02ShellWithConsolePrompts.main(arg);
		// E05X11Forwarding.main(arg);
		// E03ShellWithGUIPrompts.main(arg);
		// E04ExecuteCommand.main(arg);
		// E06LocalForwarding.main(arg);
//		E06bLocalForwardingAndShell.main(arg);
		// E07RemoteForwarding.main(arg);
		 E10PublicKeyAuthentication.main(arg);
//		 E11KeyboardInteractiveAuthentication.main(arg);
		// E14HostKeyManagement.main(arg);
		// E15SCP.main(arg);
//		 E08Sftp.main(arg);
		// E17TunneledSocketFactory.main(arg);
		// E18IdentityManagement.main(arg);
		// E19ShellUsingGSSAPI.main(arg);
	}
}
