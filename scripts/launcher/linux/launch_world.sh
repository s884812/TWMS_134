#!/bin/sh
export CLASSPATH=".:lib/*"
java -server \
-Drecvops=recvops.properties \
-Dsendops=sendops.properties \
-Dwzpath=xml \
-Dworld.config=world.properties \
-Djavax.net.ssl.keyStore=odinsea.keystore \
-Djavax.net.ssl.keyStorePassword=odinsea \
-Djavax.net.ssl.trustStore=odinsea.keystore \
-Djavax.net.ssl.trustStorePassword=odinsea \
server.Start WORLD