import net.sf.sshapi.DefaultProviderFactory;
import net.sf.sshapi.SshClient;
import net.sf.sshapi.SshConfiguration;
import net.sf.sshapi.SshProvider;

/**
 * The {@link DefaultProviderFactory} will only return a provider that supports
 * SSH version 1 (which in practice is currently only the 'Maverick16'
 * implementation)
 * 
 */
public class E09SSH1Only {
	/**
	 * Entry point.
	 * 
	 * @param arg command line arguments
	 * @throws Exception
	 */
	public static void main(String[] arg) throws Exception {
		SshConfiguration config = new SshConfiguration();
		config.setProtocolVersion(SshConfiguration.SSH1_ONLY);

		// Create the client using that configuration
		SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
		SshClient client = provider.createClient(config);
		ExampleUtilities.dumpClientInfo(client);
	}
}
