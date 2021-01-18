#!/bin/bash

TOP=$(cd $(dirname "$0") && pwd)
cd "$TOP/.."

mvn clean package

cd "$TOP"

cp -r ../sh .
cp -r ../target/*.jar rocktest.jar

docker build -t rocktest .

rm -rf sh
rm rocktest.jar

