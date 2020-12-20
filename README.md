# tuto-ssl

A brief tuto to explain ssl by example. A very simple program to see some SSL errors ans anderstand them.

SSL Exchange messages :

1. **Client:  Client hello** : The client sends information to the server, including the highest version of SSL that it supports and a list of cipher suites that it supports. (TLS 1.0 is indicated as SSL 3.1.) The cipher suite information includes cryptographic algorithms and key sizes.

2. **Server: Server hello** : The server chooses the highest version of SSL and the best cipher suite that it and the client support and sends this information to the client.

3. **Server: Certificate** : The server sends the client a certificate or a chain of certificates. A certificate chain typically begins with the server's public key certificate and ends with the root certificate of the CA. This message is optional, but is used whenever server authentication is required.

4. **Server: Certificate request** : If the server needs to authenticate the client, it sends the client a certificate request. In Internet applications, this message is rarely sent.

5. **Server: Server key exchange** : The server sends the client a server key exchange message when the public key information sent in message 3 above is not sufficient for the key exchange. For example, in Diffie-Hellman-based cipher suites, this message contains the server's DH public key.

6. **Server: Server hello done** : The server tells the client that it has completed its initial negotiation messages.

7. **Client:  Certificate** : If the server requests a certificate from the client in message 4, the client sends its certificate chain, just as the server did in message 3. Note: Only a few Internet server applications request a certificate from the client.

8. **Client:  Client key exchange** : The client generates information used to create a key to be used for symmetric encryption. For RSA, the client then encrypts this key information with the server's public key and sends it to the server. For Diffie-Hellman-based cipher suites, this message contains the client's DH public key.

9. **Client:  Certificate verify** : This message is sent when a client presents a certificate as explained previously. Its purpose is to allow the server to complete the client authentication process. When this message is used, the client sends information that it digitally signs using a cryptographic hash function. When the server decrypts this information with the client's public key, the server is able to authenticate the client.

10. **Client:  Change cipher spec** : The client sends a message telling the server to switch to encrypted mode.

11. **Client:  Finished** : The client indicates to the server that it is ready to begin secure data communication.

12. **Server: Change cipher spec** : The server sends a message telling the client to switch to encrypted mode.

13. **Server: Finished** : The server indicates to the client that it is ready to begin secure data communication. This is the end of SSL negotiation.

14. **Client:  Encrypted data** : The client and the server communicate by using the symmetric encryption algorithm and cryptographic hash function negotiated in messages 1 and 2, and by using the secret key that the client sent to the server in message 8. The handshake can be renegotiated at this time. See the next section for more details.

15. **Client:  Close Messages** : At the end of the connection, each side will send a close_notify message to inform the peer that the connection is closed.



Create some certificats for the server and the client:

- The server 

  - Creating a keystore server with a self-signed certificate:

   ```
keytool -genkey -keyalg RSA -keypass password -storepass password -keystore jks.server.keystore -alias serveur
   ```
    N.B. : Be careful to fill in the name of the machine as CN (for example localhost)

  - Extracting the keystore server certificate:

   ```
  keytool -export -storepass password -keystore jks.server.keystore -file serveur.cer -alias serveur
   ```
  
  - Adding the server certificate to the client truststore (the client knows the server):
  
   ```
  keytool -import -v -trustcacerts -keypass password -storepass password -file serveur.cer -keystore jks.client.truststore -alias serveur
   ```




- The Client:
  - Creating a client keystore with a self-signed certificate:

   ```
  keytool -genkey -keyalg RSA -keypass password -storepass password -keystore jks.client.keystore -alias client
   ```
  
  - Extracting the client keystore certificate:
  
   ```
  keytool -export -storepass password -keystore jks.client.keystore -file client.cer -alias client
   ```
  
  - Adding the client certificate to the server truststore (the server knows the client) :
  
   ```
  keytool -import -v -trustcacerts -keypass password -storepass password -file client.cer -keystore jks.server.truststore -alias client
   ```



2 java classes :

- ssl.SimpleServer : an ssl server
- ssl.SimpleClient : the client



**Launch the server :**

Usage : java ssl.SimpleServer [clientAuthentication] [knownClient] [certificatPath] [debugEnable]

\- clientAuthentication (true/false)      : Bidirectionnel SSL Handshake (mandatory)

\- knownClient (true/false)            		: server knows the client ? (mandatory)

\- certificatPath (String)                 		: keystores path (default : .)

\- debugEnable (true/false)            		: debug enable (default : false)



**Launch the client:**

Usage : java ssl.SimpleClient [knownClient] [certificatPath] [debugEnable]

\- knownServer (true/false)  	: The client knows the server ? (mandatory)

\- certificatPath (String)     		: keystores path (default : .)

\- debugEnable (true/false)  	: debug enable (default : false)



Change the parameters (*clientAuthentication*, *knownClient* and *knownServer*) to simulate ssl errors.

Activate the debug mode to see the SSL Exchange messages.

| Need client authentication (*clientAuthentication)* | Server  knows Client (*knownClient*) | Client  knows Server (*knownServer*) | Result                                                       |
| --------------------------------------------------- | ------------------------------------ | ------------------------------------ | ------------------------------------------------------------ |
| No                                                  | No                                   | No                                   | KO : <br>-       Server : Received fatal alert:  certificate_unknown  <br>-       Client : unable to find valid certification  path to requested target |
| No                                                  | No                                   | Yes                                  | OK                                                           |
| No                                                  | Yes                                  | No                                   | KO :   <br>-       Server : Received fatal alert:  certificate_unknown  <br>-       Client : unable to find valid certification  path to requested target |
| No                                                  | Yes                                  | Yes                                  | OK                                                           |
| Yes                                                 | No                                   | No                                   | KO :   <br/>-       Server : Received fatal alert:  certificate_unknown  <br/>-       Client : unable to find valid certification  path to requested target |
| Yes                                                 | No                                   | Yes                                  | KO :   <br/>-       Server : null cert chain  <br/>-       Client : Software caused connection abort:  socket write error |
| Yes                                                 | Yes                                  | No                                   | KO :   <br/>-       Server : Received fatal alert:  certificate_unknown  <br/>-       Client : unable to find valid certification  path to requested target |
| Yes                                                 | Yes                                  | Yes                                  | OK                                                           |

We launch the server and the client in the directory where the certificates are located.

Let's try to put ourselves in the second case :

The server:

```
$ java ssl.SimpleServer false false
```

```
clientAuthentication : false
knownClient : false
Current certificats path is : W:\security\ssl\ssl\target\classes\.\
Debug enable is false
############## SERVER : SSLServerSocketFactory.getDefault()...
############## SERVER : factory initialized...
############## Set the SERVER SSLServerSocket...
############## SERVER SSLServerSocket ok
############## SERVER : get the client response...
############## SERVER : Hello Client, i am the Server !
```

The Client

```
$ java ssl.SimpleClient true
```

```
knownServer : true
Current certificats path is : W:\security\ssl\ssl\target\classes\.\
Debug enable is false
############## CLIENT SocketFactory.getDefault()...
############## CLIENT factory initialized...
############## Set the CLIENT SSLSocket...
############## CLIENT SSLSocket OK
############## Print CLIENT to the output...
############## CLIENT output OK!
```

All it's ok. Let's try the first case.

The server

```
$ java ssl.SimpleServer false false
```

```
clientAuthentication : false
knownClient : false
Current certificats path is : W:\security\ssl\ssl\target\classes\.\
Debug enable is false
############## SERVER : SSLServerSocketFactory.getDefault()...
############## SERVER : factory initialized...
############## Set the SERVER SSLServerSocket...
############## SERVER SSLServerSocket ok
############## SERVER : get the client response...
############## START SERVER ERROR : Output expected SSLHandshakeExceptions
javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown
        at java.base/sun.security.ssl.Alert.createSSLException(Alert.java:131)
        ...
        at ssl.SimpleServer.main(SimpleServer.java:23)
############## END SERVER ERROR : Output expected SSLHandshakeExceptions
```

The Client

```
$ java ssl.SimpleClient false
```

```
knownServer : false
Current certificats path is : W:\security\ssl\ssl\target\classes\.\
Debug enable is false
############## CLIENT SocketFactory.getDefault()...
############## CLIENT factory initialized...
############## Set the CLIENT SSLSocket...
############## CLIENT SSLSocket OK
############## Print CLIENT to the output...
############## CLIENT output OK!
############## START CLIENT ERROR : Output expected SSLHandshakeExceptions
javax.net.ssl.SSLHandshakeException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        at java.base/sun.security.ssl.Alert.createSSLException(Alert.java:131)
        ...
        at ssl.SimpleClient.main(SimpleClient.java:21)
Caused by: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        at java.base/sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:384)
        ...
        ... 18 more
Caused by: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        ...
        at java.base/sun.security.validator.PKIXValidator.doBuild(PKIXValidator.java:379)
        ... 23 more
############## END CLIENT ERROR : Output expected SSLHandshakeExceptions
```

The server certificate is unknown from the client

That's all folks !