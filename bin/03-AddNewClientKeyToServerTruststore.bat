@echo off

call 01-SetEnvSSL.bat

set CLIENT_NAME=%1
if ""==%CLIENT_NAME% goto ERREUR1

echo ***************************************************
echo *** Creation of couples Certificates / Keys ... ***
echo ***************************************************
echo * Creation Certificates / Keys for the client %CLIENT_NAME% ...
echo -----------------------------------------------------------------
keytool -genkey -v -keystore %DB_DIR%jks.client.keystore -alias %CLIENT_NAME% -storepass %CLIENT_STORE_PWD% -keypass %CLIENT_KEY_PWD% -dname "CN=%CLIENT_NAME%, OU=Bureau Web, O=PenPen, L=Paris, S=IDF, C=FR" -validity 360 -keyalg %ALGO% -keysize %KEY_SIZE%
echo.

echo ***********************************
echo *** Exports of Certificates ... ***
echo ***********************************
echo * Exporting the client certificate ...
echo --------------------------------------
keytool -export -keystore %DB_DIR%jks.client.keystore -file %DB_DIR%jks.%CLIENT_NAME%.certificat -keypass %CLIENT_KEY_PWD% -storepass %CLIENT_STORE_PWD% -alias %CLIENT_NAME% 
echo.

echo **************************************************
echo *** import of Certificates into trutstores ... ***
echo **************************************************
echo * importation du certificat client dans le truststore server jks.server.truststore...
echo -------------------------------------------------------------------------------------
keytool -import -file %DB_DIR%jks.%CLIENT_NAME%.certificat -keystore %DB_DIR%jks.server.truststore -keypass %CLIENT_KEY_PWD% -storepass %SERVER_STORE_PWD% -alias %CLIENT_NAME% -noprompt -trustcacerts -v
echo.

echo *********************************************************
echo * Contents of the Stores for server authentication ...  *
echo *********************************************************
echo * The server keystore ...
echo -------------------------
keytool -list -keystore %DB_DIR%jks.server.keystore   -storepass %SERVER_STORE_PWD% -v
echo.

echo * The client truststore ...
echo ---------------------------
keytool -list -keystore %DB_DIR%jks.client.truststore -storepass %CLIENT_STORE_PWD% -v
echo.

echo ********************************************************
echo * Contents of the Stores for client authentication ... *
echo ********************************************************
echo * The client keystore ...
echo ------------------------
keytool -list -keystore %DB_DIR%jks.client.keystore   -storepass %CLIENT_STORE_PWD% -v
echo.

echo * The server truststore ...
echo ---------------------------
keytool -list -keystore %DB_DIR%jks.server.truststore -storepass %SERVER_STORE_PWD% -v
echo.

echo **************************************
echo *** Extraction of private keys ... ***
echo **************************************
echo * Extraction of the client private key ...
echo ------------------------------------------
keytool -importkeystore -srckeystore %DB_DIR%jks.client.keystore -srcstorepass %CLIENT_STORE_PWD% -srckeypass %CLIENT_KEY_PWD% -srcalias %CLIENT_NAME%  -destalias %CLIENT_NAME%  -destkeystore %DB_DIR%pkcs12.%CLIENT_NAME%.p12 -deststoretype PKCS12 -deststorepass %CLIENT_STORE_PWD% -destkeypass %CLIENT_KEY_PWD% -v
openssl pkcs12 -in %DB_DIR%pkcs12.%CLIENT_NAME%.p12 -out %DB_DIR%jks.%CLIENT_NAME%.key -nodes -nocerts -passin pass:%CLIENT_KEY_PWD% -passout pass:%CLIENT_KEY_PWD%
echo.

echo ********************************************
echo *** Export of certificates in DER format ***
echo ********************************************
echo * Export of the client certificate in DER format into pem.%CLIENT_NAME%.certificat ...
echo --------------------------------------------------------------------------------------
%OPENSSL_CMD% x509 %C_M%in %DB_DIR%jks.%CLIENT_NAME%.certificat %C_M%out "%DB_DIR%pem.%CLIENT_NAME%.certificat" %C_M%outform pem %C_M%text %C_M%inform der
echo DER Certificat exported into %DB_DIR%pem.%CLIENT_NAME%.certificat
echo.

echo ***********************************************
echo *** Converting private keys to PEM format...***
echo ***********************************************
echo * Convertion de la clef privee cliente au format PEM ...
echo --------------------------------------------------------
copy "%DB_DIR%jks.%CLIENT_NAME%.key" "%DB_DIR%pem.%CLIENT_NAME%.key"
echo.

echo *************************************************************************************************************************************
echo *** Export of the Certificates/Key pair in Binary format (DER) to PKCS12 format into pkcs12.%CLIENT_NAME%.pfx for the browser ... ***
echo *************************************************************************************************************************************
%OPENSSL_CMD% pkcs12 %C_M%export %C_M%in "%DB_DIR%pem.%CLIENT_NAME%.certificat" %C_M%out "%DB_DIR%pkcs12.%CLIENT_NAME%.pfx" %C_M%inkey "%DB_DIR%pem.%CLIENT_NAME%.key" %C_M%name "Certificat %CLIENT_NAME% de PenPen" %C_M%certfile "%DB_DIR%pem.%CLIENT_NAME%.certificat" %C_M%password pass:%SERVER_KEY_PWD%:%CLIENT_KEY_PWD%
%OPENSSL_CMD% pkcs12 -info -in %DB_DIR%pkcs12.%CLIENT_NAME%.pfx %C_M%password pass:%SERVER_KEY_PWD%:%CLIENT_KEY_PWD%  %C_M%passout pass:%CLIENT_KEY_PWD%
echo Certificats/Clef exported into %DB_DIR%pkcs12.%CLIENT_NAME%.pfx
echo.

echo ***********************************************
echo *** Merging of %CLIENT_NAME% certificates : ***
echo ***********************************************
%JAVA% -cp %CLASSPATH_SSL% utils.UtilConcatFile "%DB_DIR%pem.%CLIENT_NAME%.certificat" "%DB_DIR%pem.AC.certificat"

del /F /Q "%DB_DIR%jks.%CLIENT_NAME%.certificat"
del /F /Q "%DB_DIR%jks.%CLIENT_NAME%.key"
del /F /Q "%DB_DIR%pem.%CLIENT_NAME%.certificat"
del /F /Q "%DB_DIR%pem.%CLIENT_NAME%.key"

echo off

goto RETOUR

:ERREUR1
ECHO "Set the client"
goto RETOUR

:RETOUR
