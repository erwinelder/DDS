#!/bin/bash
set -e

cd ../

./gradlew clean build -x test

for ip in 192.168.64.8 192.168.64.9 192.168.64.10 192.168.64.11 192.168.64.12; do \
  scp ~/Documents/DDS/build/libs/DDS-all.jar debian@$ip:. ; \
  ssh debian@$ip 'docker stop $(docker ps -q) || true && docker rm -f dds 2>/dev/null || true && docker rmi -f dds:latest 2>/dev/null || true && docker build -t dds:latest .'
done

trap 'kill $(jobs -p)' SIGINT SIGTERM

for ip in 192.168.64.8 192.168.64.9 192.168.64.10 192.168.64.11 192.168.64.12; do \
  echo "Starting node on $ip..."

  ssh debian@$ip "docker run -e NODE_ADDRESS=$ip -p 8080:8080 dds" &
done

wait