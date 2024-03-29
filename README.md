# SSHAPI

SSHAPI is a clean and modern API for accessing SSH servers in Java. However, it does not supply the support for the protocol itself, or any encryption code, it instead delegates this to an *SSH Provider*, which in turn uses one of many already available SSH libraries.

```java
	try (SshClient client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
		try(SftpClient sftp = client.sftp()) {
			sftp.get("/home/myhome/stuff.txt", new File("stuff.txt"));
		}
	}
```

It was originally written with two purposes in mind.

 * We wanted a way to compare SSH APIs for performance to highlight issues in our own products.
 * An abstraction layer for our UniTTY product to work with multiple SSH libraries.
 
However, it is now a viable complete SSH api with other advantages :-

 * Adds a modern API over the top of legacy APIs (e.g. try-with-resource support and non-blocking I/O).
 * If a new cipher becomes fashionable, and one provider implements it first, in many cases you can simply switch to it   with no code changes. 
 * If a vulnerability is discovered in one library, switch to another, again just by swapping the provider bridge in use.
 * If you have different performance or security requirements based on configuration, you can leave the choice to users
 * Different SSH libraries have different licenses, give yourself or your users choices based on these concerns.
 * You can compare yourself the performance or behaviour of one API against another.
 * Get near native SSH performance using the libssh provider (work in progress).
 * If the provider supports non-blocking usage, it will be used, otherwise SSHAPI will simulate non-blocking usage.
 
## Requirements

 * Java. As of version 2.0.0, SSHAPI requires Java 11
 * Maven if building from source.

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

 * sshapi-maverick-synergy (Modern, open source JAdaptive [Maverick Synergy](https://jadaptive.com/en/products/open-source-java-ssh) API)
 * sshapi-maverick-synergy-hotfixes ([Commercially supported](https://jadaptive.com/en/products/maverick-synergy/pricing) version of [Maverick Synergy](https://jadaptive.com/en/products/open-source-java-ssh))
 * sshapi-maverick16  (Commercial, Legacy JAdaptive [Maverick](https://jadaptive.com/en/products/java-ssh-client) API)
 * sshapi-sshj (Currently well maintained open source [SSHJ](https://github.com/hierynomus/sshj) API)
 * sshapi-jsch (Now uses an [updated fork](https://github.com/mwiede/jsch) of this well established API)
 * sshapi-trilead (A fork of Ganymed, itself now apparently unmaintained)
 * sshapi-ganymed (Now apparently unmaintained)
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

### Connecting, Authenticating and Creating A Shell

```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var shell = client.shell()) {
		var in = shell.getInputStream();
		var out = shell.getOutputStream();
		
		// Do something with I/O streams
	}
}

```

### Connecting, Authenticating and Creating A Shell with a Pseudo Tty

```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var shell = client.shell("xterm", 80, 24, 0, 0, null)) {
		var in = shell.getInputStream();
		var out = shell.getOutputStream();
		
		// Do something with I/O streams
	}
}

```

### Getting A Remote File using SFTP
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var sftp = client.sftp()) {
		sftp.get("/home/myhome/stuff.txt", new File("stuff.txt"));
	}
}
```

### Uploading A File using SCP
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var scp = client.scp()) {
		scp.put("remote-name", null, new File("stuff.txt"), false);
	}
}
```

### Listing A Directory

There are a few ways of doing this.

#### As A Complete Array

All files will be loaded into memory at once. 
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var sftp = client.sftp()) {
		for(var file : sftp.ls("/home/myhome/stuff.txt")) {
			System.out.println(file.getName());
		}
	}
}
```

#### As A Stream

For iterating over large directories.
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var sftp = client.sftp()) {
		try(var stream = client.directory("/home/myhome")) {
			for(var file : stream) {
				System.out.println(file.getName());
			}				
		}
	}
}
```

#### Recursively

Use a `FileVisitor` to recursively process an entire tree of files.

```
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var sftp = client.sftp()) {
		sftp.visit("/home/myhome", new SftpFileVisitor() {
			@Override
			public FileVisitResult visitFile(SftpFile file, BasicFileAttributes attrs) throws IOException {
				System.out.println(file.getName());
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
```

### Random Access Read And Write
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var sftp = client.sftp()) {
	
		// Write test file
		try (var handle = sftp.file("test-file.txt", Mode.SFTP_WRITE, Mode.SFTP_CREAT)) {
			ByteBuffer buf = ByteBuffer.allocate(16);
			buf.putInt(1);
			buf.putInt(2);
			buf.putInt(3);
			buf.putInt(4);
			buf.flip();
			handle.writeTo(buf);
		}
			
		// Read 4 bytes from test file from 4th byte, which is the 2nd 'int' written above
		try (var handle = sftp.file("test-file.txt", Mode.SFTP_READ)) {
			ByteBuffer buf = ByteBuffer.allocate(4);
			handle.position(4).readFrom(buf);
			if(buf.getInt(0) != 2)
				throw new IllegalStateException("Expected to receive value of 2.");
		}
	}
}
```

### Run A Remote Command And Get The Output
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var command = client.command("ls /etc")) {
		Util.joinStreams(command.getOutputStream(), System.out);
		System.out.println("Exited with code: " + command.exitCode());
	}
}
```

### Start A Local Port Forward, Giving You Access To Remote TCP Service (e.g. web server)
 
```java
try (var client = Ssh.open("me@localhost", new ConsolePasswordAuthenticator())) {
	try(var command = client.localForward("0.0.0.0", 8443, "someremoteaddress", 443)) {
		// Simply sleep to keep the tunnel open. 
		Thread.sleep(600000);
	}
}
```

### Authenticating Using a Public/Private Keys
 
```java
// Prompt for location of private key
var pemFile = new File(Util.prompt("Private key file",
		System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa"));
			
try (var client = Ssh.open("me@localhost", 
		new DefaultPublicKeyAuthenticator(new ConsolePasswordAuthenticator(), pemFile))) {
	// Do SSH stuff
}
```

### Custom Configuration

```java
var config = new SshConfiguration();
config.addRequiredCapability(Capability.SFTP);
config.setPreferredServerToClientMAC("hmac-sha1");
config.setHostKeyValidator(new ConsoleHostKeyValidator());
config.setBannerHandler(new ConsoleBannerHandler());

// Create the client using that configuration and connect and authenticate
try (var client = config.open("me@localhost", new ConsolePasswordAuthenticator()) {
	// Do SSH stuff
}
```

### Multiple Providers

```java
// List all of the providers and allow the user to select one
var providers = DefaultProviderFactory.getAllProviders();
System.out.println("Providers :-");
for (int i = 0; i < providers.length; i++) {
	System.out.println("  " + (i + 1) + ": " + providers[i].getClass().getName());
}
var provider = providers[Integer.parseInt(Util.prompt("\nEnter the number for the provider you wish to use (1-"
	+ providers.length + ")")) - 1];
	
// Create a client using that provider
try (var client = provider.open(new SshConfiguration(), "me@localhost", new ConsolePasswordAuthenticator()) {
	// Do SSH stuff
}
```

### Changing A Private Key Passphrase

```java

// Need a provider that does IDENTITY_MANAGEMENT
var config = new SshConfiguration();
config.addRequiredCapability(Capability.IDENTITY_MANAGEMENT);

// Create the provider, then identity manager using that configuration
var provider = DefaultProviderFactory.getInstance().getProvider(config);
System.out.println("Got provider " + provider.getClass());
var mgr = provider.createIdentityManager(config);

// Read and (optionally) decrypt key	
SshPrivateKeyFile pk;
var keyFile = new File("/home/myhome/.ssh/id_rsa");
try(var in = new FileInputStream(keyFile)) {
	pk = mgr.createPrivateKeyFromStream(in);			
	if (pk.isEncrypted()) {
		var pw = Util.prompt("Old passphrase");
		pk.decrypt(pw.toCharArray());
	}
}
	
// Change passphrase
pk.changePassphrase(newpw.toCharArray());

// Write the key back out
try(var fout = new FileOutputStream(keyFile)) {
	fout.write(pk.getFormattedKey());
}
	
```

### Create A Socket Factory That Is Tunneled To a Remote Host

```java
var config = new SshConfiguration();
config.addRequiredCapability(Capability.TUNNELED_SOCKET_FACTORY);

// Connect, authenticate
try (var client = config.open("me@localhost", new ConsolePasswordAuthenticator())) {

	var sf = client.createTunneledSocketFactory();

	/*
	 * Make a connection back to the SSH server we are connecting from and read the
	 * first line of output. This could be any host that is accessible from the
	 * remote SSH server, localhost:22 is just used as we know something will be
	 * running there!
	 */
	try(var socket = sf.createSocket("localhost", 22)) {
		var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		System.out.println("SSH ident: " + reader.readLine());
	}
}
```

### Authenticating Using A Local SSH Agent

```java
var config = new SshConfiguration();
config.addRequiredCapability(Capability.AGENT);

try (var client = config.open("me@localhost", new DefaultAgentAuthenticator())) {
	// Do SSH stuff
}
```

### Non-blocking usage

SSHAPI can use a non-blocking pattern if you prefer that. For most methods, there will be an equivalent **Later** method. For example `shell()` has a `shellLater()`. These methods will not block, nor will they throw an exception. Instead, a `Future` is returned, that may be used to retrieve the shell object when it is ready, or cancel the operation. If an exception occurs during the operation, the future will also return that.

#### Non-blocking Shell

```java
var future = Ssh.openLater("me@localhost", new ConsolePasswordAuthenticator())

// At some point after this you can attempt to retrieve the SshClient instance from the future. This will block until it's available. You can request a timeout too
var client = future.get(10, TimeUnit.SECONDS);

// Shells act in the same way
var shellFuture = client.shellLater();
var shell = shellFuture.get(); 

// To handle data coming from the remote server, set the input handler
shell.setInput((buffer) -> {
	// Do something with ByteBuffer (buffer is pre-flipped to limit() will be length of data, position() will be zero
});

// To send data to the shell simple use writeLater(). You use the future to 
// wait for when this actually happens.
var writeFuture = shell.writeLater(ByteBuffer.wrap("Test!".getBytes()));
writeFuture.get();

// Now you can close, and of course, wait for the close to complete if you want
var closeShellFuture = shell.closeLater();
closeShellFuture.get();

// And finally close the client too
var closeFuture = client.closeLater();
closeFuture.get();
```