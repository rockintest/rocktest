#!/bin/bash

TOP=$(cd $(dirname "$0") && pwd)
cd "$TOP/.."

mvn clean package

cd "$TOP"

cp ../target/rocktest-bin.tar.gz .

docker build -t rocktest .

rm rocktest-bin.tar.gz
