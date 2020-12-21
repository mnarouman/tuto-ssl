package ssl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SimpleClient {
	private static final String HOSTNAME = "localhost";
	private static boolean IS_DEBUG_ENABLE = false;
	
	public static void main(String[] args) throws IOException {
		setSslProperties(args);
		startClient(HOSTNAME, SimpleServer.SERVER_PORT);
	}

	private static void setSslProperties(String[] args) {
		if (args.length < 1) {
			usage();
		}

		boolean knownServer = false;
		// knownServer
		try {
			knownServer = getBoolean(args[0]);
			System.out.println("knownServer : "+ knownServer);
		} catch (Exception e) {
			System.err.println("Error on knownServer");
			usage();
		}
		
		// certificatPath
		String rootResources = null;
		if (args.length > 1) {
			File certificatsPath = new File(args[1]);
			if (certificatsPath.exists()) {
				rootResources = certificatsPath.getAbsolutePath();
			} else {
				System.err.println("The directory "+ args[2] + " does not exists");
				usage();
			}
		} else {
			rootResources = new File(".").getAbsolutePath();
		}
		rootResources = rootResources.endsWith(File.separator) ? rootResources : rootResources+File.separator;
		System.out.println("Current certificats path is : "+ rootResources);
		
		// debugEnable
		if (args.length > 2) {
			try {
				IS_DEBUG_ENABLE = getBoolean(args[2]);
			} catch (Exception e) {
				System.err.println("ERROR : The seconde argument, debugEnable, must be true or false.");
			}
		}
		System.out.println("Debug enable is " + IS_DEBUG_ENABLE);

		System.setProperty("javax.net.ssl.keyStore", rootResources + "jks.client.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");

		// Does the CLient knows the Sever ?
		if (knownServer) {
			System.setProperty("javax.net.ssl.trustStore", rootResources + "jks.client.truststore");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
		}

		if (IS_DEBUG_ENABLE) {
			System.setProperty("javax.net.debug", "ssl:all");
			System.setProperty("java.security.debug", "access:stack");
		}
		
		// JAVA_HOME
		System.out.println("La valeur de java.home est :" + System.getProperty("java.home"));
		
		// JSSE
		try {
			Class.forName("com.sun.net.ssl.internal.ssl.Provider");
			System.out.println("JSSE est correctement installe !");
		} catch (Exception e) {
			System.out.println("JSSE n est pas installe correctement !");
		}
		
		// TRUSTSTORE
		String trustStore = System.getProperty("javax.net.ssl.trustStore");
		if (trustStore == null) {
			System.out.println("javax.net.ssl.trustStore n est pas defini.");
		} else {
			System.out.println("javax.net.ssl.trustStore = " + trustStore);
		}

	}

	private static void usage() {
		System.err.println("Usage : java ssl.SimpleClient [knownClient] [certificatPath] [debugEnable]");
		System.err.println("- knownServer (true/false) 	: The client knows the server ? (mandatory)");
		System.err.println("- certificatPath (String) 	: keystores path (default : .)");
		System.err.println("- debugEnable (true/false) 	: debug enable (default : false)");
		System.exit(1);
	}
	
	private static boolean getBoolean(String arg) throws Exception {
		boolean b;
		if (arg != null && ("true".equals(arg.toLowerCase()) || "false".equals(arg.toLowerCase()))) {
			b = Boolean.valueOf(arg);
		} else {
		  throw new Exception();
		}
		return b;
	}

	static void startClient(String host, int port) throws IOException {
		System.out.println("############## CLIENT SocketFactory.getDefault()...");
		SocketFactory factory = SSLSocketFactory.getDefault();
		System.out.println("############## CLIENT factory initialized...");

		try (Socket connection = factory.createSocket(host, port)) {
			if (IS_DEBUG_ENABLE) {
				sslInfos(connection);
			}
			
			System.out.println("############## Set the CLIENT SSLSocket...");
			((SSLSocket) connection).setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"});
			((SSLSocket) connection).setEnabledProtocols(new String[] { "TLSv1.2" });
			SSLParameters sslParams = new SSLParameters();
			sslParams.setEndpointIdentificationAlgorithm("HTTPS");
			((SSLSocket) connection).setSSLParameters(sslParams);
			System.out.println("############## CLIENT SSLSocket OK");
			
			OutputStream outputStream = ((SSLSocket) connection).getOutputStream();
			try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)){
				System.out.println("############## Print CLIENT to the output...");
				outputStreamWriter.write("Client");
				System.out.println("############## CLIENT output OK!");
			}
			
		}catch (SSLHandshakeException exception) {
			System.err.println("############## START CLIENT ERROR : Output expected SSLHandshakeExceptions");
            exception.printStackTrace();
            System.err.println("############## END CLIENT ERROR : Output expected SSLHandshakeExceptions");
        } catch (IOException exception) {
        	System.err.println("############## START CLIENT ERROR : Output unexpected InterruptedExceptions and IOExceptions");
        	exception.printStackTrace();
        	System.err.println("############## END CLIENT ERROR : Output unexpected InterruptedExceptions and IOExceptions");
        }
	}

	private static void sslInfos(Socket connection) {
		System.out.println("############## START CLIENT SSL INFOS ###############");
		System.out.println("Ciphers suite enable");
		String[] enabledCipherSuites = ((SSLSocket) connection).getEnabledCipherSuites();
		for (String cipher : enabledCipherSuites) {
			System.out.println(cipher);
		}
		
		System.out.println();
		System.out.println("Protocols enable");	
		String[] enabledProtocols = ((SSLSocket) connection).getEnabledProtocols();
		for (String protocol : enabledProtocols) {
			System.out.println(protocol);	
		}
		System.out.println("############## END CLIENT SSL INFOS ###############");
	}
}
