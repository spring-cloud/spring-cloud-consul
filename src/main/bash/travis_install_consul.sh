#!/bin/bash

CONSUL_VER="0.6.3"
CONSUL_ZIP="consul_${CONSUL_VER}_linux_amd64.zip"
# cleanup
rm "consul_*"
rm "consul"
# install consul
wget "https://releases.hashicorp.com/consul/${CONSUL_VER}/${CONSUL_ZIP}
unzip ${CONSUL_ZIP}
# check
./consul --version