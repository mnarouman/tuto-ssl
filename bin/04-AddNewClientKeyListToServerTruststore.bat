@echo on

call 01-SetEnvSSL.bat

rem del /F /Q %DB_DIR%*.*

call 03-AddNewClientKeyToServerTruststore.bat papa
call 03-AddNewClientKeyToServerTruststore.bat maman

@echo off
