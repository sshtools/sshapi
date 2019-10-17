# SSHAPI

SSHAPI is a clean and modern API for accessing SSH servers in Java. However, it does not supply the support for the protocol itself, or any encryption code, it instead delegates this to an *SSH Provider*, which in turn uses one of many already available SSH libraries.

It was originally written with two purposes in mind.

 * We wanted a way to compare SSH APIs for performance to highlight issues in our own products.
 * An abstraction layer for our UniTTY product to work with multiple SSH libraries.
 
However, it is now a viable complete SSH api with other advantages :-

 * Adds a modern API over the top of legacy APIs (e.g. try-with-resource support).
 * If a new cipher becomes fashionable, and one provider implements it first, in many cases you can simply switch to it   with no code changes. 
 * If a vulnerability is discovered in one library, switch to another, again just by swapping the provider bridge in use.
 * If you have different performance or security requirements based on configuration, you can leave the choice to users
 * Different SSH libraries have different licenses, give yourself or your users choices based on these concerns.
 * You can compare yourself the performance or behaviour of one API against another.
 * Get near native SSH performance using the libssh provider (work in progress).
 
## Installation

Installation of SSHAPI is no harder than any other SSH library for Java. Using your chosen build tool (below we use Maven), add the appropriate *Provider Bridge* library to your project, and all of the appropriate dependencies will be pulled in.

```xml
	
	<dependency>
		<groupId>com.sshtools</groupId>
		<artifactId>sshapi-maverick-synergy</artifactId>
		<version>2.0.0-SNAPSHOT</version>
		<scope>compile</scope>
	</dependency>
```

Providers include :-

 * sshapi-maverick-synergy
 * sshapi-maverick16
 * sshapi-jsch
 * sshapi-trilead
 * sshapi-ganymed
 * sshapi-j2ssh (more or less obsolete as uses old ciphers and key exchange)
 * sshapi-libssh (experimental)
 * sshapi-openssh (incomplete experimental)

NOTE, if you are using SNAPSHOT versions of the library, you will need to add the Sonatype OSS snapshot repository too.

```xml
	<repository>
		<id>oss-snapshots</id>
		<url>https://oss.sonatype.org/content/repositories/snapshots</url>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots />
	</repository>
```

## Usage

For full usage, see the examples.

### Connecting, Authenticating and Creating A Shell with a Pseudo Tty

```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(SshShell shell = client.shell("xterm", 80, 24, 0, 0, null)) {
			InputStream in = shell.getInputStream();
			OutputStream out = shell.getOutputStream();
			
			// Do something with I/O streams
		}
	}

```

### Getting A Remote File using SFTP
 
```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(SftpClient sftp = client.sftp()) {
			sftp.get("/home/myhome/stuff.txt", new File("stuff.txt"));
		}
	}
```

### Uploading A File using SCP
 
```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(ScpClient scp = client.scp()) {
			scp.put("remote-name", null, new File("stuff.txt"), false);
		}
	}
```

### Run A Remote Command And Get The Output
 
```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(SshCommand command = client.command("ls /etc")) {
			Util.joinStreams(command.getOutputStream(), System.out);
			System.out.println("Exited with code: " + command.exitCode());
		}
	}
```

### Start A Local Port Forward, Giving You Access To Remote TCP Service (e.g. web server)
 
```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(SshPortForward command = client.localForward("0.0.0.0", 8443, "someremoteaddress", 443)) {
			// Simply sleep to keep the tunnel open. 
			Thread.sleep(600000);
		}
	}
```

### Authenticating Using a Public/Private Keys
 
```java

	// Prompt for location of private key
	File pemFile = new File(Util.prompt("Private key file",
			System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"));
				
	try (SshClient client = Ssh.open("me@localhost", 
			new DefaultPublicKeyAuthenticator(new ConsolePasswordAuthenticator(), pemFile))) {
		// Do SSH stuff
	}
```

### Custom Configuration

```java

	SshConfiguration config = new SshConfiguration();
	config.addRequiredCapability(Capability.SFTP);
	config.setPreferredServerToClientMAC("hmac-sha1");
	config.setHostKeyValidator(new ConsoleHostKeyValidator());
	config.setBannerHandler(new ConsoleBannerHandler());

	// Create the client using that configuration and connect and authenticate
	try (SshClient client = config.open("me@localhost", new ConsolePasswordAuthenticator()) {
		// Do SSH stuff
	}
				
```

### Multiple Providers

```java
	// List all of the providers and allow the user to select one
	SshProvider[] providers = DefaultProviderFactory.getAllProviders();
	System.out.println("Providers :-");
	for (int i = 0; i < providers.length; i++) {
		System.out.println("  " + (i + 1) + ": " + providers[i].getClass().getName());
	}
	SshProvider provider = providers[Integer.parseInt(Util.prompt("\nEnter the number for the provider you wish to use (1-"
		+ providers.length + ")")) - 1];
		
	// Create a client using that provider
	try (SshClient client = provider.open(new SshConfiguration(), "me@localhost", new ConsolePasswordAuthenticator()) {
		// Do SSH stuff
	}
```

### Changing A Private Key Passphrase

```java

	// Need a provider that does IDENTITY_MANAGEMENT
	SshConfiguration config = new SshConfiguration();
	config.addRequiredCapability(Capability.IDENTITY_MANAGEMENT);

	// Create the provider, then identity manager using that configuration
	SshProvider provider = DefaultProviderFactory.getInstance().getProvider(config);
	System.out.println("Got provider " + provider.getClass());
	SshIdentityManager mgr = provider.createIdentityManager(config);

	// Read and (optionally) decrypt key	
	SshPrivateKeyFile pk;
	File keyFile = new File("/home/myhome/.ssh/id_rsa");
	try(FileInputStream in = new FileInputStream(keyFile)) {
		pk = mgr.createPrivateKeyFromStream(in);			
		if (pk.isEncrypted()) {
			String pw = Util.prompt("Old passphrase");
			pk.decrypt(pw.toCharArray());
		}
	}
		
	// Change passphrase
	pk.changePassphrase(newpw.toCharArray());

	// Write the key back out
	try(FileOutputStream fout = new FileOutputStream(keyFile)) {
		fout.write(pk.getFormattedKey());
	}
	
```

### Create A Socket Factory That Is Tunneled To a Remote Host

```java
	SshConfiguration config = new SshConfiguration();
	config.addRequiredCapability(Capability.TUNNELED_SOCKET_FACTORY);

	// Connect, authenticate
	try (SshClient client = config.open("me@localhost", new ConsolePasswordAuthenticator())) {

		SocketFactory sf = client.createTunneledSocketFactory();

		/*
		 * Make a connection back to the SSH server we are connecting from and read the
		 * first line of output. This could be any host that is accessible from the
		 * remote SSH server, localhost:22 is just used as we know something will be
		 * running there!
		 */
		try(Socket socket = sf.createSocket("localhost", 22)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			System.out.println("SSH ident: " + reader.readLine());
		}
	}
```

### Authenticating Using A Local SSH Agent

```java
	SshConfiguration config = new SshConfiguration();
	config.addRequiredCapability(Capability.AGENT);

	try (SshClient client = config.open("me@localhost", new DefaultAgentAuthenticator())) {
		// Do SSH stuff
	}
```