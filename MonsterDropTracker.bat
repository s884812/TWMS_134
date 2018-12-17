@echo off
set CLASSPATH=.;lib\*
java -Drecvops=recvops.ini -Dsendops=sendops.ini -Dwzpath=xml\ tools.MonsterDropTracker 2049001
pause