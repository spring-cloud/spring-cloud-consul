#!/bin/bash

./src/main/bash/travis_install_consul.sh
./src/test/bash/travis_run_consul.sh
./mvnw clean install -B -Pdocs ${@}
