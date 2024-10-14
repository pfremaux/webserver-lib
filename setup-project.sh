#!/bin/bash

# Only commands should be:
# curl https://github.com/pfremaux/webserver-lib/blob/main/setup-project.sh -o webserver-setup.sh
# chmod u+x webserver-setup.sh
# ./webserver-setup.sh
# SOURCES_URL = "https://github.com/pfremaux/webserver-lib/blob/main/build.sh"

ask_with_default() {
  echo $1[$2]
  read -s secret
  if [ -z $secret ]
  then
    returnValue=$2
    return 1
  fi
  returnValue=$secret
  return 0
}


GIT_SOURCES = "git@github.com:pfremaux/webserver-lib.git"

git clone $GIT_SOURCES
cd webserver-lib
chmod u+x ./build.sh
cd ..

ask_with_default "Project name" "example"
mkdir $returnValue

cd $returnValue
cp webserver-lib/server-lib.jar $returnValue/
cp webserver-lib/server-config.properties $returnValue/

# TODO PFR confirm delete webserver lib rm -Rf webserver-lib/

cd $returnValue
mkdir -p src/main/java
mkdir -p src/main/web

