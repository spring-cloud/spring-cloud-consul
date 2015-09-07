#!/bin/bash
consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -ui-dir `dirname $0`/../../../src/test/resources/consul_ui -config-dir `dirname $0`/../../test/resources/consul_config &
# wait for consul to elect a leader before sending acl
sleep 5000
curl -X PUT -d @`dirname $0`/../../test/resources/consul_acl/consul_discovery_client_acl.json http://localhost:8500/v1/acl/create?token=2ee647bd-bd69-4118-9f34-b9a6e9e60746