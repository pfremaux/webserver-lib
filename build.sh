#!/bin/bash
find . -name *.java > sources.txt
javac @sources.txt -d ./build/classes/java/main

jar cf server-lib.jar -C build/classes/java/main .
