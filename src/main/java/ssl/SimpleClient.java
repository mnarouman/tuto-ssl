package ssl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.net.SocketFactory;
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
			System.err.println("Erreur sur knownServer");
			usage();
		}
		
		// certificatPath
		String rootResources = null;
		if (args.length > 1) {
			File certificatsPath = new File(args[1]);
			if (certificatsPath.exists()) {
				rootResources = certificatsPath.getAbsolutePath();
			} else {
				System.err.println("Le repertoire "+ args[2] + " n'existe pas");
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

		if (knownServer) {
			System.setProperty("javax.net.ssl.trustStore", rootResources + "jks.client.truststore");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
		}

		if (IS_DEBUG_ENABLE) {
			System.setProperty("javax.net.debug", "ssl:all");
			System.setProperty("java.security.debug", "access:stack");
		}
	}

	private static void usage() {
		System.err.println("Usage : java SimpleClient.class clientAuthentication knownClient certificatPath debugEnable");
		System.err.println(" knownServer (true/false) : Est-ce que le client connait le server ?");
		System.err.println(" certificatPath (String) : Chemin des keystores (default : .");
		System.err.println(" debugEnable (true/false) : activer le debug (default : false");
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
		SocketFactory factory = SSLSocketFactory.getDefault();

		try (Socket connection = factory.createSocket(host, port)) {
			if (IS_DEBUG_ENABLE) {
				sslInfos(connection);
			}
			((SSLSocket) connection).setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"});
			((SSLSocket) connection).setEnabledProtocols(new String[] { "TLSv1.2" });
			SSLParameters sslParams = new SSLParameters();
			sslParams.setEndpointIdentificationAlgorithm("HTTPS");
			((SSLSocket) connection).setSSLParameters(sslParams);
			
			OutputStream outputStream = ((SSLSocket) connection).getOutputStream();
			PrintWriter out = new PrintWriter(outputStream, true);
			out.println("Client1");

		}
	}

	private static void sslInfos(Socket connection) {
		System.out.println();
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
		System.out.println();
	}

}
