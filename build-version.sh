#!/bin/bash
# TODO PFR validate input parameter
git tag $1
find . -name *.java > sources.txt
javac @sources.txt -d ./build/classes/java/main

jar cf server-lib-$1.jar -C build/classes/java/main .
