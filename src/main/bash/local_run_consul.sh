#!/bin/bash
mkdir /tmp/consul-config
consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -config-dir=/tmp/consul-config -ui-dir `dirname $0`/../../../src/test/resources/consul_ui
