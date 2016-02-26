#!/bin/bash

set -eo pipefail
#OLD_VERSION=1.0.0.BUILD-SNAPSHOT
#NEW_VERSION=1.0.0.M6-v2
OLD_VERSION=$(sed -n '/<version>/ {s/\s*<.\?version>//gp;q;}' pom.xml )
NEW_VERSION=${OLD_VERSION%%-*}-v$((${OLD_VERSION##*-v}  + 1))

grep -rl "$OLD_VERSION" --include=pom.xml|xargs -n1 sed -i "s/$OLD_VERSION/$NEW_VERSION/g"
mvn deploy -Dmaven.test.skip=true -DaltDeploymentRepository=aws-release::default::file://$PWD/maven-repo
cd maven-repo
aws s3 cp --region eu-west-1  --acl=public-read  --recursive org s3://maven.sequenceiq.com/releases/org/
