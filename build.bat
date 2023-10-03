dir /s /B src\main\*.java > sources.txt
javac @sources.txt -d ./build/classes/java/main

jar cf server-lib.jar -C build\classes\java\main .
jar fum server-lib.jar manifest.txt