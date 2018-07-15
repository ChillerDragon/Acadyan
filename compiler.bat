@echo off

::javac src\krisko\acadyan\*.java src\krisko\acadyan\gui\*.java src\krisko\socketconnection\*.java ::build class files

:compile
cls
echo compiling Acaydan...
echo ==============================
echo creating class files in build folder...
javac -d ./build src\krisko\acadyan\*.java src\krisko\acadyan\gui\*.java src\krisko\socketconnection\*.java
echo entering build folder...
cd build
echo creating .jar file...
jar cf Acadyan.jar *
echo moving file too root directory...
move Acadyan.jar ..\

echo ==============================
echo done. Press any key to compile agian
pause >NUL
goto compile