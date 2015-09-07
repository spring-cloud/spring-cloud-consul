#!/bin/bash
mkdir /tmp/consul-config
BASEDIR=`dirname $0`/../../..
consul agent -server -bootstrap-expect 1 -advertise 127.0.0.1 -data-dir /tmp/consul -config-dir=/tmp/consul-config -ui-dir ${BASEDIR}/src/test/resources/consul_ui
