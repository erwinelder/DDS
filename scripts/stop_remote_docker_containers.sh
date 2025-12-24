#!/bin/bash
set -e

cd ../

for ip in 192.168.64.3 192.168.64.4 192.168.64.5 192.168.64.6 192.168.64.7; do \
  ssh debian@$ip 'docker stop $(docker ps -q) || true'
done