@echo off
set JAVA_HOME=j:\jdk1.8.0_144\jre
set JAVA=%JAVA_HOME%\bin\java.exe
set DB_DIR=%CD%\ssl\
mkdir %DB_DIR%
set OPENSSL_DIR=c:\cygwin64\bin\
set OPENSSL_CMD=%OPENSSL_DIR%openssl.exe
set C_M=-

set ALGO=RSA
set KEY_SIZE=2048

set HOST_NAME_SERVER=localhost
set HOST_NAME_CLIENT=client

set SERVER_STORE_PWD=password
set SERVER_KEY_PWD=%SERVER_STORE_PWD%
set CLIENT_STORE_PWD=password
set CLIENT_KEY_PWD=%CLIENT_STORE_PWD%

set CLASSPATH_SSL=%JAVA_HOME%/lib/rt.jar;%JAVA_HOME%/lib/jsse.jar;SSLTool.jar;.;swt.jar;commons-codec-1.2.jar;commons-httpclient-3.0-alpha1.jar;commons-logging.jar

set OPTION_SERVER_SSL=-Djavax.net.ssl.keyStore=jks.server.keystore -Djavax.net.ssl.keyStorePassword=%SERVER_STORE_PWD% -Djavax.net.ssl.keyPassword=%SERVER_KEY_PWD%
set OPTION_SERVER_SSL=-Djavax.net.ssl.trustStore=jks.server.truststore -Djavax.net.ssl.trustStorePassword=%SERVER_STORE_PWD% %OPTION_SERVER_SSL%

set OPTION_CLIENT_SSL=-Djavax.net.ssl.keyStore=jks.client.keystore -Djavax.net.ssl.keyStorePassword=%CLIENT_STORE_PWD%
set OPTION_CLIENT_SSL=-Djavax.net.ssl.trustStore=jks.client.truststore -Djavax.net.ssl.trustStorePassword=%CLIENT_STORE_PWD% %OPTION_CLIENT_SSL%

rem set OPTION_DEBUG_SSL=-Djava.security.debug=all %OPTION_DEBUG_SSL%
rem set OPTION_DEBUG_SSL=-Djavax.net.debug=all %OPTION_DEBUG_SSL%
