#!/bin/bash
set -e

trap 'kill $(jobs -p)' SIGINT SIGTERM

for ip in 192.168.64.3 192.168.64.4 192.168.64.5 192.168.64.6 192.168.64.7; do \
  echo "Starting node on $ip..."

  ssh debian@$ip 'docker stop $(docker ps -q) || true'
  ssh debian@$ip "docker run -e NODE_ADDRESS=$ip -p 8080:8080 dds" &
done

wait