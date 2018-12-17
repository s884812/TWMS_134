@echo off
set CLASSPATH=.;lib\*
java -Dodinms.recvops=recvops.ini -Dodinms.sendops=sendops.ini -Dodinms.wzpath=xml\ tools.MonsterDropCreator false
pause