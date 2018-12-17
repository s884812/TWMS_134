@echo off
set CLASSPATH=.;lib\*
java -server -Dwzpath=xml\ -Djavax.net.ssl.keyStore=odinsea.keystore -Djavax.net.ssl.keyStorePassword=odinsea -Djavax.net.ssl.trustStore=odinsea.keystore -Djavax.net.ssl.trustStorePassword=odinsea server.Start WORLD
pause