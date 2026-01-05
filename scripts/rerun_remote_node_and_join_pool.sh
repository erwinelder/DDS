#!/bin/bash
set -e

cd ../

node_ip="192.168.64.3"
greeter_node_ip="192.168.64.4"

ssh debian@$node_ip 'docker stop $(docker ps -q) && docker rm -f dds 2>/dev/null || true && docker rmi -f dds:latest 2>/dev/null || true && docker build -t dds:latest .'
ssh debian@$node_ip "docker run -d -e NODE_ADDRESS=$node_ip -p 8080:8080 dds"

sleep 1

curl -X POST http://$node_ip:8080/Node/join \
  -H "Content-Type: application/json" \
  -d "{
        \"0\": \"$greeter_node_ip\"
      }"