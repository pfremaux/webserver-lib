#!/bin/bash
# TODO PFR main is more the library and Example could be converted/split  to extract a main/
# /main/ would contain almost only the runner
rm -Rf ./build/classes/java/main
find -path './src/main/*.java' -name *.java > sources.txt
javac @sources.txt -d ./build/classes/java/main

jar cf server-lib.jar -C build/classes/java/main .
jar fum server-lib.jar manifest.txt