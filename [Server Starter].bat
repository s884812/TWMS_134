@echo off
color 77
cls

start /b scripts\launcher\launch_world.bat
@title 1/4 Activing

ping -n 6 127.0.0.1>nul

start /b scripts\launcher\launch_login.bat
@title 2/4 Activing

ping -n 8 127.0.0.1>nul

start /b scripts\launcher\launch_channel.bat
@title 3/4 Activing

ping -n 8 127.0.0.1>nul

start /b scripts\launcher\launch_cs.bat
@title 4/4 Activing

@title Server Fully Active