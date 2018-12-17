@echo off
set CLASSPATH=.;lib\*
java -Xms10m -Xmx512m -server -Dchannel.config=settings.ini -Dwzpath=xml\ -Djavax.net.ssl.keyStore=odinsea.keystore -Djavax.net.ssl.keyStorePassword=odinsea -Djavax.net.ssl.trustStore=odinsea.keystore -Djavax.net.ssl.trustStorePassword=odinsea server.Start CHANNEL -Dcom.sun.management.jmxremote.port=13373 -Dcom.sun.management.jmxremote.password.file=jmxremote.password -Dcom.sun.management.jmxremote.access.file=jmxremote.access
pause