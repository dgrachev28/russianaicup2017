@echo off

rd /Q /S classes
md classes

call "%KOTLIN_HOME%\bin\kotlinc.bat" src/main/java -d classes
javac -cp "classes;%KOTLIN_HOME%\lib\kotlin-stdlib.jar" -encoding UTF-8 -sourcepath "src/main/java" -d classes "src/main/java/Runner.java" > compilation.log
echo %CLASSPATH%

if not exist classes\Runner.class (
    echo Unable to find classes\Runner.class >> compilation.log
    exit /b 1
)

if not exist classes\MyStrategy.class (
    echo Unable to find classes\MyStrategy.class >> compilation.log
    exit /b 1
)

jar cvfe "./kotlin-cgdk.jar" Runner -C "./classes" .
