#!/bin/sh
export CLASSPATH=".:lib/*"
java -Xms10m -Xmx512m -server \
-Drecvops=recvops.properties \
-Dsendops=sendops.properties \
-Dwzpath=xml \
-Dchannel.config=channel.properties \
-Djavax.net.ssl.keyStore=odinsea.keystore \
-Djavax.net.ssl.keyStorePassword=odinsea \
-Djavax.net.ssl.trustStore=odinsea.keystore \
-Djavax.net.ssl.trustStorePassword=odinsea \
server.Start CHANNEL