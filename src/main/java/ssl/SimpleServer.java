package ssl;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SimpleServer {
	public static final int SERVER_PORT = 8443;
	private static boolean IS_DEBUG_ENABLE = false;
	public static boolean CLIENT_AUTHENTICATION_REQUIERED = false;

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		setSslProperties(args);
		startServer(SERVER_PORT);
	}

	private static void setSslProperties(String[] args) {
		if (args.length < 2) {
			usage();
		}

		// clientAuthentication
		try {
			CLIENT_AUTHENTICATION_REQUIERED = getBoolean(args[0]);
			System.out.println("clientAuthentication : "+ CLIENT_AUTHENTICATION_REQUIERED);
		} catch (Exception e) {
			System.err.println("Erreur sur clientAuthentication");
			usage();
		}

		// knownClient
		boolean knownClient = false;
		try {
			knownClient = getBoolean(args[1]);
			System.out.println("knownClient : "+ knownClient);
		} catch (Exception e) {
			System.err.println("Erreur sur knownClient");
			usage();
		}
		
		// certificatPath
		String rootResources = null;
		if (args.length > 2) {
			File certificatsPath = new File(args[2]);
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
		if (args.length > 3) {
			try {
				IS_DEBUG_ENABLE = getBoolean(args[3]);
			} catch (Exception e) {
				System.err.println("ERROR : The seconde argument, debugEnable, must be true or false.");
			}
		}
		System.out.println("Debug enable is " + IS_DEBUG_ENABLE);

		System.setProperty("javax.net.ssl.keyStore", rootResources + "jks.server.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "password");

		if (knownClient) {
			System.setProperty("javax.net.ssl.trustStore", rootResources + "jks.server.truststore");
			System.setProperty("javax.net.ssl.trustStorePassword", "password");
		}

		if (IS_DEBUG_ENABLE) {
			System.setProperty("javax.net.debug", "ssl:all");
			System.setProperty("java.security.debug", "access:stack");
		}
	}

	private static void usage() {
		System.err.println("Usage : java SimpleServer.class clientAuthentication knownClient certificatPath debugEnable");
		System.err.println(" clientAuthentication (true/false) : Handshake SSL bidirectionnel");
		System.err.println(" knownClient (true/false) : Est-ce que le server connait le client ?");
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

	static void startServer(int port) throws IOException, NoSuchAlgorithmException {
		ServerSocketFactory factory = SSLServerSocketFactory.getDefault();

		try (ServerSocket listener = factory.createServerSocket(port)) {
			if (IS_DEBUG_ENABLE) {
				sslInfos(listener);
			}
			((SSLServerSocket) listener).setNeedClientAuth(CLIENT_AUTHENTICATION_REQUIERED);
			((SSLServerSocket) listener).setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384" });
			((SSLServerSocket) listener).setEnabledProtocols(new String[] { "TLSv1.2" });
			// while (true) {
			try (Socket socket = listener.accept()) {
				try (Scanner response = new Scanner(socket.getInputStream())) {
					if (response.hasNextLine()) {
						String clientName = response.next();
						System.out.println("Hello " + clientName);
					}
				}
			}
		}
	}

	private static void sslInfos(ServerSocket listener) {
		System.out.println();
		System.out.println("Ciphers suite enable");
		String[] enabledCipherSuites = ((SSLServerSocket) listener).getEnabledCipherSuites();
		for (String cipher : enabledCipherSuites) {
			System.out.println(cipher);
		}

		System.out.println();
		System.out.println("Protocols enable");
		String[] enabledProtocols = ((SSLServerSocket) listener).getEnabledProtocols();
		for (String protocol : enabledProtocols) {
			System.out.println(protocol);
		}
		System.out.println();
	}
}