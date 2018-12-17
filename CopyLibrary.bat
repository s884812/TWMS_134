@echo off
title Copy Library
color 77
xcopy /y /h "lib\bcprov-jdk16-145.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\bcprov-jdk16-145.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

xcopy /y /h "lib\jeval.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\jeval.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

xcopy /y /h "lib\mina-core.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\mina-core.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

xcopy /y /h "lib\mysql-connector-java-bin.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\mysql-connector-java-bin.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

xcopy /y /h "lib\slf4j-api.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\slf4j-api.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

xcopy /y /h "lib\slf4j-jdk14.jar" "C:\Program Files (x86)\Java\jdk1.6.0_11\jre\lib\ext"
xcopy /y /h "lib\slf4j-jdk14.jar" "C:\Program Files (x86)\Java\jre6\lib\ext"

echo Done.
pause
exit