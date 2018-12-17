@echo off
set CLASSPATH=.;lib\*
java -server -Dlogin.config=settings.ini -Dwzpath=xml\ -Djavax.net.ssl.keyStore=odinsea.keystore -Djavax.net.ssl.keyStorePassword=odinsea -Djavax.net.ssl.trustStore=odinsea.keystore -Djavax.net.ssl.trustStorePassword=odinsea server.Start LOGIN
pause