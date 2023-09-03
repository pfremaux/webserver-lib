FROM alpine:latest

RUN apk update
RUN apk add openjdk17
ADD server-lib.jar /
ADD https.sh /
ADD key-store.jks /
ADD server-config.properties /
ENTRYPOINT ./https.sh
