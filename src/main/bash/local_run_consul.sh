#!/bin/bash
consul agent -server -bootstrap-expect 1 -data-dir /tmp/consul -ui-dir `dirname $0`/../../../consul_ui
