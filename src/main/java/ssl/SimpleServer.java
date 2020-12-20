package ssl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLHandshakeException;
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
			System.err.println("Error on clientAuthentication");
			usage();
		}

		// knownClient
		boolean knownClient = false;
		try {
			knownClient = getBoolean(args[1]);
			System.out.println("knownClient : "+ knownClient);
		} catch (Exception e) {
			System.err.println("Error on knownClient");
			usage();
		}
		
		// certificatPath
		String rootResources = null;
		if (args.length > 2) {
			File certificatsPath = new File(args[2]);
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

		// Does the server knows the client ?
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
		System.err.println("Usage : java ssl.SimpleServer [clientAuthentication] [knownClient] [certificatPath] [debugEnable]");
		System.err.println("- clientAuthentication (true/false)	: Handshake SSL bidirectionnel (mandatory)");
		System.err.println("- knownClient (true/false)		: server knows the client ? (mandatory)");
		System.err.println("- certificatPath (String)		: keystores path (default : .)");
		System.err.println("- debugEnable (true/false)		: debug enable (default : false)");
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
		System.out.println("############## SERVER : SSLServerSocketFactory.getDefault()...");
		ServerSocketFactory factory = SSLServerSocketFactory.getDefault();
		System.out.println("############## SERVER : factory initialized...");

		try (ServerSocket listener = factory.createServerSocket(port)) {
			if (IS_DEBUG_ENABLE) {
				sslInfos(listener);
			}
			System.out.println("############## Set the SERVER SSLServerSocket...");
			((SSLServerSocket) listener).setNeedClientAuth(CLIENT_AUTHENTICATION_REQUIERED);
			((SSLServerSocket) listener).setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384" });
			((SSLServerSocket) listener).setEnabledProtocols(new String[] { "TLSv1.2" });
			System.out.println("############## SERVER SSLServerSocket ok");
			
			try (Socket socket = listener.accept()) {
				System.out.println("############## SERVER : get the client response...");
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String clientName = in.readLine();
				System.out.println("############## SERVER : Hello " + clientName + ", i  am the Server!");
				
			} catch (SSLHandshakeException exception) {
				System.err.println("############## START SERVER ERROR : Output expected SSLHandshakeExceptions");
	            exception.printStackTrace();
	            System.err.println("############## END SERVER ERROR : Output expected SSLHandshakeExceptions");
	        } catch (IOException exception) {
	        	System.err.println("############## START SERVER ERROR : Output unexpected InterruptedExceptions and IOExceptions");
	        	exception.printStackTrace();
	        	System.err.println("############## END SERVER ERROR : Output unexpected InterruptedExceptions and IOExceptions");
	        }
		}
	}

	private static void sslInfos(ServerSocket listener) {
		System.out.println("############## START SERVER SSL INFOS ###############");
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
		System.out.println("############## END SERVER SSL INFOS ###############");
	}
}