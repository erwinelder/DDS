#!/bin/bash
set -e

cd ../

if [ $# -ne 1 ]; then
  echo "Usage: $0 <VM_IP>"
  exit 1
fi

VM_IP="$1"

ssh "debian@$VM_IP" 'docker stop $(docker ps -q)'