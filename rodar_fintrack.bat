@echo off
chcp 65001 > nul
set JAVA_TOOL_OPTIONS=

cd /d C:\Users\Pedro\Desktop\FinTrack\FinTrack

javac -encoding UTF-8 -d build\classes src\app\Main.java src\Controller\*.java src\model\*.java src\utils\*.java src\exceptions\*.java

java -cp build\classes app.Main

pause