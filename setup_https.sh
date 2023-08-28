#!/bin/bash
DEV_MODE=1

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

ask_secret_with_default() {
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


ask_secret_with_default "Key password? (Not required on startup but keep it somewhere)" "CHANGEME"
keyPassword=$returnValue
ask_secret_with_default "Keystore password? (must be provided in CLI)" "CHANGEME"
keyStorePassword=$returnValue
ask_with_default "File name? (must be provided in CLI)" "key-store.jks"
fileName=$returnValue

keytool -genkeypair -keyalg RSA -alias selfsigned -keystore $fileName -storepass $keyStorePassword -keypass $keyPassword -validity 360 -keysize 2048 -deststoretype pkcs12


if [ $DEV_MODE -eq 1 ]
then
  echo #!/bin/bash > dev_run_https.sh
  echo ./https.sh $keyStorePassword TOKEN_PASSWORD > dev_run_https.sh
  chmod u+x dev_run_https.sh
fi