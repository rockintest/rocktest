#!/bin/bash

echo "$*"

TOP=$(cd $(dirname "$0") && pwd)

cd $TOP
./build.sh

docker tag rocktest rockintest/rocktest:latest
docker push rockintest/rocktest:latest

if [ -n "$1" ]; then
  docker tag rocktest rockintest/rocktest:$1
  docker push rockintest/rocktest:$1
fi
