#!/bin/bash

echo "$*"

TOP=$(cd $(dirname "$0") && pwd)

cd $TOP
./build.sh

docker tag rocktest benoittouron/rocktest:latest
docker push benoittouron/rocktest:latest

if [ -n "$1" ]; then
  docker tag rocktest benoittouron/rocktest:$1
  docker push benoittouron/rocktest:$1
fi
