@echo off

call 01-SetEnvSSL.bat

del /F /Q %DB_DIR%*.*

echo ***************************************************
echo *** Creation of couples Certificates / Keys ... ***
echo ***************************************************
echo * Creation Certificates / Keys for the server ...
echo ------------------------------------------------
keytool -genkey -v -keystore %DB_DIR%jks.server.keystore -alias serveur -storepass %SERVER_STORE_PWD% -keypass %SERVER_KEY_PWD% -dname "CN=%HOST_NAME_SERVER%, OU=Bureau Web, O=PenPen, L=Paris, S=IDF, C=FR" -validity 360 -keyalg %ALGO% -keysize %KEY_SIZE%
echo.
echo * Creation Certificates / Keys for the client ...
echo -----------------------------------------------
keytool -genkey -v -keystore %DB_DIR%jks.client.keystore -alias client  -storepass %CLIENT_STORE_PWD% -keypass %CLIENT_KEY_PWD% -dname "CN=%HOST_NAME_CLIENT%, OU=Bureau Web, O=PenPen, L=Paris, S=IDF, C=FR" -validity 360 -keyalg %ALGO% -keysize %KEY_SIZE%
echo.

echo ***********************************
echo *** Exports of Certificates ... ***
echo ***********************************
echo * Exporting the server certificate ...
echo ---------------------------------------
keytool -export -keystore %DB_DIR%jks.server.keystore -file %DB_DIR%jks.AS.certificat -keypass %SERVER_KEY_PWD% -storepass %SERVER_STORE_PWD% -alias serveur  
echo.
echo * Exporting the client certificate ...
echo --------------------------------------
keytool -export -keystore %DB_DIR%jks.client.keystore -file %DB_DIR%jks.AC.certificat -keypass %CLIENT_KEY_PWD% -storepass %CLIENT_STORE_PWD% -alias client
echo.

echo ***********************************************************
echo *** import of Certificates into trutstores ... ***
echo ***********************************************************
echo * importing the server certificate into the client truststore jks.client.truststore...
echo --------------------------------------------------------------------------------------
keytool -import -file %DB_DIR%jks.AS.certificat -keystore %DB_DIR%jks.client.truststore -keypass %SERVER_KEY_PWD% -storepass %CLIENT_STORE_PWD% -alias serveur -noprompt -trustcacerts -v
echo.
echo * importing the client certificate into the truststore server jks.server.truststore ...
echo -------------------------------------------------------------------------------------
keytool -import -file %DB_DIR%jks.AC.certificat -keystore %DB_DIR%jks.server.truststore -keypass %CLIENT_KEY_PWD% -storepass %SERVER_STORE_PWD% -alias client -noprompt -trustcacerts -v
echo.

echo **************************************
echo *** Extraction of private keys ... ***
echo **************************************
echo * Extraction of the server private key ...
echo ------------------------------------------
keytool -importkeystore -srckeystore %DB_DIR%jks.server.keystore -destkeystore %DB_DIR%pkcs12.AS.p12 -srcstorepass %SERVER_STORE_PWD% -srckeypass %SERVER_KEY_PWD% -srcalias serveur -destalias serveur -deststoretype PKCS12 -deststorepass %SERVER_STORE_PWD% -destkeypass %SERVER_KEY_PWD% -v
openssl pkcs12 -in %DB_DIR%pkcs12.AS.p12 -nodes -nocerts -out %DB_DIR%jks.AS.key -passin pass:%SERVER_KEY_PWD% -passout pass:%SERVER_KEY_PWD%
echo.
echo * Extraction of the client private key ...
echo ------------------------------------------
keytool -importkeystore -srckeystore %DB_DIR%jks.client.keystore -destkeystore %DB_DIR%pkcs12.AC.p12 -srcstorepass %CLIENT_STORE_PWD% -srckeypass %CLIENT_KEY_PWD% -srcalias client  -destalias client -deststoretype PKCS12 -deststorepass %CLIENT_STORE_PWD% -destkeypass %CLIENT_KEY_PWD% -v
openssl pkcs12 -in %DB_DIR%pkcs12.AC.p12 -nodes -nocerts -out %DB_DIR%jks.AC.key -passin pass:%CLIENT_KEY_PWD% -passout pass:%CLIENT_KEY_PWD%
echo.
rem %JAVA% -cp %CLASSPATH_SSL% com.penpen.portail.ssl.UtilExportPriv %DB_DIR%jks.server.keystore serveur  %SERVER_KEY_PWD% %SERVER_STORE_PWD% > %DB_DIR%jks.AS.key
rem %JAVA% -cp %CLASSPATH_SSL% com.penpen.portail.ssl.UtilExportPriv %DB_DIR%jks.client.keystore client   %CLIENT_KEY_PWD% %CLIENT_STORE_PWD% > %DB_DIR%jks.AC.key
echo.

echo *********************************************************
echo * Contents of the Stores for server authentication ...  *
echo *********************************************************
echo * The server keystore ...
echo -------------------------
keytool -list -keystore %DB_DIR%jks.server.keystore   -storepass %SERVER_STORE_PWD% -v
echo.

echo * The client truststore ...
echo --------------------------
keytool -list -keystore %DB_DIR%jks.client.truststore -storepass %CLIENT_STORE_PWD% -v
echo.

echo ***********************************************
echo * Store content for client authentication ... *
echo ***********************************************
echo * The client keystore ...
echo ------------------------
keytool -list -keystore %DB_DIR%jks.client.keystore   -storepass %CLIENT_STORE_PWD% -v
echo.

echo * The server truststore ...
echo ---------------------------
keytool -list -keystore %DB_DIR%jks.server.truststore -storepass %SERVER_STORE_PWD% -v
echo.

echo ********************************************
echo *** Export of certificates in DER format ***
echo ********************************************
echo * Exporting the server certificate in DER format in pem.AS.certificat ...
echo -------------------------------------------------------------------------
%OPENSSL_CMD% x509 %C_M%in %DB_DIR%jks.AS.certificat %C_M%out "%DB_DIR%pem.AS.certificat" %C_M%outform pem %C_M%text %C_M%inform der
echo DER Certificat exported into %DB_DIR%pem.AS.certificat
echo.
echo * Export of the client certificate in DER format in pem.AC.certificat ...
echo --------------------------------------------------------------------------
%OPENSSL_CMD% x509 %C_M%in %DB_DIR%jks.AC.certificat %C_M%out "%DB_DIR%pem.AC.certificat" %C_M%outform pem %C_M%text %C_M%inform der
echo DER Certificat exported into %DB_DIR%pem.AC.certificat
echo.

echo *************************************************
echo *** Converting private keys to PEM format ... ***
echo *************************************************
echo * Converting the server private key to PEM format ...
echo -----------------------------------------------------
copy "%DB_DIR%jks.AS.key" "%DB_DIR%pem.AS.key"
echo.
echo * Conversion of the client private key to PEM format ...
echo --------------------------------------------------------
copy "%DB_DIR%jks.AC.key" "%DB_DIR%pem.AC.key"
echo.

echo ****************************************************************************************************************************
echo *** Export of the Certificates / Key pair in Binary format (DER) to PKCS12 format into pkcs12.AC.pfx for the browser ... ***
echo ****************************************************************************************************************************
%OPENSSL_CMD% pkcs12 %C_M%export %C_M%in "%DB_DIR%pem.AC.certificat" %C_M%out "%DB_DIR%pkcs12.AC.pfx" %C_M%inkey "%DB_DIR%pem.AC.key"   %C_M%name "Certificat de client"   %C_M%certfile "%DB_DIR%pem.AC.certificat" %C_M%password pass:%SERVER_KEY_PWD%:%CLIENT_KEY_PWD%
%OPENSSL_CMD% pkcs12 -info -in %DB_DIR%pkcs12.AC.pfx %C_M%password pass:%SERVER_KEY_PWD%:%CLIENT_KEY_PWD%  %C_M%passout pass:%CLIENT_KEY_PWD%
echo Certificats/key exported into %DB_DIR%pkcs12.AC.pfx
echo.

del /F /Q "%DB_DIR%jks.AS.certificat"
del /F /Q "%DB_DIR%jks.AS.key"
del /F /Q "%DB_DIR%jks.AC.certificat"
del /F /Q "%DB_DIR%jks.AC.key"
del /F /Q "%DB_DIR%pem.AC.key"

echo ***************************
echo *** End of key creation ***
echo ***************************

rem pause