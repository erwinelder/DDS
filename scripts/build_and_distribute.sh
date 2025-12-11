#!/bin/bash
set -e

./gradlew clean build

for ip in 192.168.64.3 192.168.64.4; do \
  scp ~/Documents/DDS/build/libs/DDS-all.jar debian@$ip:. ; \
  ssh debian@192.168.64.3 "docker rm -f dds 2>/dev/null || true && docker rmi -f dds:latest 2>/dev/null || true && docker build -t dds:latest ."
done