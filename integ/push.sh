#!/bin/bash

TOP=$(cd $(dirname "$0") && pwd)

cd $TOP
./build.sh

docker tag rocktest benoittouron/rocktest:latest
docker push benoittouron/rocktest:latest
