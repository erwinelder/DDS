#!/bin/bash
set -e

cd ../

ip="192.168.64.3"

ssh debian@$ip 'docker stop $(docker ps -q) && docker rm -f dds 2>/dev/null || true && docker rmi -f dds:latest 2>/dev/null || true && docker build -t dds:latest .'
ssh debian@$ip "docker run -d -e NODE_ADDRESS=$ip -p 8080:8080 dds"