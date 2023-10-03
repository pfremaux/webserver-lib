#!/bin/bash
rm -Rf ./build/classes/java/main
find -path './src/main/*.java' -name *.java > sources.txt
javac @sources.txt -d ./build/classes/java/main

jar cf server-lib.jar -C build/classes/java/main .
jar fum server-lib.jar manifest.txt