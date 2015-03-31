#!/bin/bash

CONSUL_VER="0.5.0_linux_amd64"
# cleanup
rm "${CONSUL_VER}.*"
rm "consul"
# install consul
wget "https://dl.bintray.com/mitchellh/consul/${CONSUL_VER}.zip"
unzip "${CONSUL_VER}.zip"
# check
./consul --version