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
package net.sf.sshapi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation of a {@link SshProviderFactory} that by default
 * searches for known implementations on the classpath. It can also be
 * configured using system properties.
 * <p>
 * <h2>Locating providers</h2>
 * 
 */
public class DefaultProviderFactory implements SshProviderFactory {
	private static final Logger LOG = SshConfiguration.getLogger();
	/**
	 * System property name that may be set to use a specific provider
	 */
	public final static String PROVIDER_CLASS_NAME = "net.sf.sshapi.provider";
	// Private constants
	private final static String FACTORY_CLASS_NAME = "net.sf.sshapi.factory";
	// Private statics
	private static ClassLoader providerClassLoader = null;
	private final static Map<String, SshProvider> providerCache = new HashMap<>();

	/**
	 * Constructor
	 */
	public DefaultProviderFactory() {
	}

	/**
	 * Get a singleton instance of the client factory. The instance will be created
	 * on the first call to this method. By default, an instance of this class will
	 * be created, although this may be overridden by setting the system property
	 * {@link #FACTORY_CLASS_NAME}.
	 * 
	 * @return instance
	 */
	public static SshProviderFactory getInstance() {
		return DefaultClientFactoryHolder.instance;
	}

	/**
	 * Get all discovered providers. See above for details on how providers are
	 * discovered.
	 * 
	 * @return all providers
	 */
	public static SshProvider[] getAllProviders() {
		List<SshProvider> providers = new ArrayList<>();
		ClassLoader cl = getClassLoader();
		try {
			for (Enumeration<URL> e = cl.getResources("sshapi-providers.properties"); e.hasMoreElements();) {
				try {
					addIfNotExist(providers, e);
				} catch (UnsatisfiedLinkError ule) {
					LOG.warn("Provider requires a native library but it could not be found.", ule);
				} catch (NoClassDefFoundError ex) {
					LOG.warn("Provider failed to load. Error suggests that an incorrect dependency library may be being used.",
							ex);
				} catch (Exception ex) {
					LOG.warn("Provider failed to load. ", ex);
				}
			}
		} catch (IOException ioe) {
			throw new IllegalStateException("Could not discover providers.", ioe);
		}
		return (SshProvider[]) providers.toArray(new SshProvider[providers.size()]);
	}

	private static void addIfNotExist(List<SshProvider> providers, Enumeration<URL> e) throws IOException {
		URL url = (URL) e.nextElement();
		SshProvider provider = loadFromProperties(url);
		if (provider != null && !providers.contains(provider)) {
			providers.add(provider);
		}
	}

	/**
	 * Get all discovered {@link Capability}. This is determine by examining all
	 * providers for their capabilities and producing a unique list.
	 * 
	 * @return all capabilities
	 */
	public static Capability[] getAllCapabilties() {
		List<Capability> capabilties = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			List<Capability> c = providers[i].getCapabilities();
			for (Iterator<Capability> it = c.iterator(); it.hasNext();) {
				Capability cap = (Capability) it.next();
				if (!capabilties.contains(cap)) {
					capabilties.add(cap);
				}
			}
		}
		return (Capability[]) capabilties.toArray(new Capability[0]);
	}

	/**
	 * Get all discovered ciphers. This is determine by examining all providers for
	 * their ciphers and producing a unique list.
	 * 
	 * @return all ciphers
	 */
	public static String[] getAllCiphers() {
		List<String> ciphers = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getSupportedCiphers(SshConfiguration.SSH1_OR_SSH2);
				addList(ciphers, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load.", ex);
			}
		}
		return ciphers.toArray(new String[0]);
	}

	/**
	 * Get all discovered key exchange algorithms. This is determine by examining
	 * all providers for their algorithms and producing a unique list.
	 * 
	 * @return all key exchange algorithms
	 */
	public static String[] getAllKEX() {
		List<String> kex = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getSupportedKeyExchange();
				addList(kex, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load. ", ex);
			}
		}
		return kex.toArray(new String[0]);
	}

	/**
	 * Get all discovered compression algorithms. This is determine by examining all
	 * providers for their algorithms and producing a unique list.
	 * 
	 * @return all compression algorithms
	 */
	public static String[] getAllCompression() {
		List<String> comp = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getSupportedCompression();
				addList(comp, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load. ", ex);
			}
		}
		return (String[]) comp.toArray(new String[0]);
	}

	/**
	 * Get all discovered message authentication code algorithms. This is determine
	 * by examining all providers for their algorithms and producing a unique list.
	 * 
	 * @return all message authentication code algorithms
	 */
	public static String[] getAllMAC() {
		List<String> mac = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getSupportedMAC();
				addList(mac, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load. ", ex);
			}
		}
		return mac.toArray(new String[0]);
	}

	/**
	 * Get a provider given it's name.
	 * 
	 * @param providerName provider name
	 * @return provider
	 * @throws IllegalArgumentException if no such provider is found
	 */
	public static SshProvider getProviderByName(String providerName) {
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			if (providers[i].getName().equals(providerName)) {
				return providers[i];
			}
		}
		throw new IllegalArgumentException("No provider named " + providerName);
	}

	/**
	 * Get all discovered public key algorithms. This is determine by examining all
	 * providers for their algorithms and producing a unique list.
	 * 
	 * @return all public key algorithms
	 */
	public static String[] getAllPublicKey() {
		List<String> pk = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getSupportedPublicKey();
				addList(pk, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load. ", ex);
			}
		}
		return pk.toArray(new String[0]);
	}

	/**
	 * Get all discovered fingerprint hashing algorithms. This is determine by
	 * examining all providers for their algorithms and producing a unique list.
	 * 
	 * @return all fingerprint hashing algorithms
	 */
	public static String[] getAllFingerprintHashingAlgorithms() {
		List<String> fps = new ArrayList<>();
		SshProvider[] providers = getAllProviders();
		for (int i = 0; i < providers.length; i++) {
			try {
				List<String> c = providers[i].getFingerprintHashingAlgorithms();
				addList(fps, c);
			} catch (UnsatisfiedLinkError ule) {
				LOG.warn("Provider requires a native library but it could not be found.", ule);
			} catch (Exception ex) {
				LOG.warn("Provider failed to load. ", ex);
			}
		}
		return fps.toArray(new String[0]);
	}

	private static void addList(List<String> kex, List<String> c) {
		for (Iterator<String> it = c.iterator(); it.hasNext();) {
			String cap = it.next();
			if (!kex.contains(cap)) {
				kex.add(cap);
			}
		}
	}

	/**
	 * Get the default provider.
	 * 
	 * @return provider
	 */
	public SshProvider getProvider() {
		return getProvider(null);
	}

	public SshProvider getProvider(SshConfiguration configuration) {
		/*
		 * First check if the provider system property is set. If it is, we use that and
		 * only that
		 */
		SshProvider provider = loadFromProperties(System.getProperties());
		if (provider != null && !provider.supportsConfiguration(null)) {
			throw new UnsupportedOperationException("The provider " + provider.getClass().getName()
					+ " requires configuration. Use createClient(SshConfiguration) instead or use a different provider.");
		}
		/*
		 * Now look for sshapi-providers.properties resources on the classpath
		 */
		if (provider == null) {
			ClassLoader cl = getClassLoader();
			try {
				LOG.info("Looking for sshapi-providers.properties resources.");
				for (Enumeration<URL> e = cl.getResources("sshapi-providers.properties"); e.hasMoreElements()
						&& provider == null;) {
					SshProvider possibleProvider = loadFromProperties(e.nextElement());
					if (possibleProvider != null) {
						if (possibleProvider.supportsConfiguration(configuration)) {
							LOG.info("Provider supports configuration.");
							provider = possibleProvider;
						} else {
							LOG.info("Provider DOES NOT support configuration.");
						}
					}
				}
			} catch (IOException ioe) {
				throw new IllegalStateException("Could not discover providers.", ioe);
			}
		}
		// There are no providers found
		if (provider == null) {
			throw new IllegalStateException(
					"No working SSH providers, or none that could satisfy configuration requirements were found on the classpath.");
		}
		return provider;
	}

	/**
	 * Set the class loader to use to load providers. By default, this will be
	 * current {@link Thread#getContextClassLoader()} if one is set, or the class
	 * loader that loaded this class if not.
	 * 
	 * @param classLoader class loader
	 */
	public static void setProviderClassLoader(ClassLoader classLoader) {
		providerClassLoader = classLoader;
	}

	protected static final SshProvider createProviderInstance(String className)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			LOG.info("Attempting to load provider {0}.", className);
			SshProvider provider = (SshProvider) (Class.forName(className, true, getClassLoader()).newInstance());
			LOG.info("Provider {0} loaded", className);
			return provider;
		} catch (InstantiationException ie) {
			if (ie.getCause() != null && ie.getCause() instanceof ClassNotFoundException) {
				throw ((ClassNotFoundException) ie.getCause());
			}
			throw ie;
		}
	}

	protected static final ClassLoader getClassLoader() {
		ClassLoader classLoader = providerClassLoader;
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		if (classLoader == null) {
			classLoader = DefaultProviderFactory.class.getClassLoader();
		}
		return classLoader;
	}

	protected static final SshProvider loadFromProperties(URL resource) throws IOException {
		LOG.info("Loading {0}", resource);
		return loadFromProperties(loadProperties(resource));
	}

	protected static final SshProvider loadFromProperties(Properties properties) {
		String requestedProviderClassName = properties.getProperty(PROVIDER_CLASS_NAME);
		if (requestedProviderClassName != null && requestedProviderClassName.length() > 0) {
			if (providerCache.containsKey(requestedProviderClassName)) {
				return ((SshProvider) providerCache.get(requestedProviderClassName));
			}
			/*
			 * The provider properties also specify a class name that the provider depends
			 * on, i.e. one that exists in the provider library, not the bridge
			 */
			String dependsOn = properties.getProperty(requestedProviderClassName + ".dependsOn", "");
			if (!dependsOn.equals("")) {
				try {
					Class.forName(dependsOn, true, getClassLoader());
				} catch (ClassNotFoundException cnfe) {
					LOG.warn(
							"The provider {0} was found, but a class it depends on ({1}, does not exist. Probably caused by a missing dependency.",
							requestedProviderClassName, dependsOn);
					return null;
				}
			}
			try {
				SshProvider provider = createProviderInstance(requestedProviderClassName);
				providerCache.put(requestedProviderClassName, provider);
				return provider;
			} catch (NoClassDefFoundError ncdfe) {
				LOG.warn("Could not load provider {0}. Probably cause by a missing dependency.",
						requestedProviderClassName);
			} catch (Exception e) {
				LOG.warn("Could not load provider {0}. {1}", requestedProviderClassName, e.getLocalizedMessage());
			}
		}
		return null;
	}

	protected static final Properties loadProperties(URL resource) throws IOException {
		Properties properties = new Properties();
		InputStream in = resource.openStream();
		try {
			properties.load(in);
		} finally {
			in.close();
		}
		return properties;
	}

	private static class DefaultClientFactoryHolder {
		private static final SshProviderFactory instance = createInstance();

		private static SshProviderFactory createInstance() {
			String factoryClassName = System.getProperty(FACTORY_CLASS_NAME, DefaultProviderFactory.class.getName());
			try {
				// Instantiating using Constructor is required because
				// obfuscation changes the access of the constructor
				Constructor<?> c = Class.forName(factoryClassName).getConstructor(new Class[] {});
				c.setAccessible(true);
				return (SshProviderFactory) c.newInstance(new Object[0]);
			} catch (Exception e) {
				throw new RuntimeException("Failed to create SSH client factory.", e);
			}
		}
	}
}
