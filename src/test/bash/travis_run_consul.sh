#!/bin/bash
ps -ef | grep consul
killall consul
BASEDIR=`dirname $0`/../../..
${BASEDIR}/consul agent -server -bootstrap-expect 1 -advertise 127.0.0.1 -data-dir /tmp/consul -ui-dir ${BASEDIR}/src/test/resources/consul_ui -config-dir ${BASEDIR}/src/test/resources/consul_config &
# wait for consul to elect a leader before sending acl
sleep 5
curl -X PUT -d @`dirname $0`/../../test/resources/consul_acl/consul_anonymous_acl.json http://localhost:8500/v1/acl/create?token=2ee647bd-bd69-4118-9f34-b9a6e9e60746
echo "consul_anonymous_acl installed"
curl -X PUT -d @`dirname $0`/../../test/resources/consul_acl/consul_discovery_client_acl.json http://localhost:8500/v1/acl/create?token=2ee647bd-bd69-4118-9f34-b9a6e9e60746
echo "consul_discovery_client_acl installed"
