#!/bin/bash

if [ -z "$1" ]
then
  echo Keystore password?
  read -s ksp
else
  ksp=$1
fi

if [ -z "$2" ]
then
  echo Token password?
  read -s tkp
else
  tkp=$2
fi


java -jar server-lib.jar --keystore-pwd $ksp --token-pwd $tkp