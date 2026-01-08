#!/bin/bash
set -e

cd ../

for ip in 192.168.64.8 192.168.64.9 192.168.64.10 192.168.64.11 192.168.64.12; do \
  ssh debian@$ip 'docker stop $(docker ps -q) || true'
done