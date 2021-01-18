#!/bin/bash

cp -r ../sh .
cp -r ../target/*.jar rocktest.jar

docker build -t rocktest .
