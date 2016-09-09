#!/usr/bin/env bash
set -e

nVersion="${GRAILS_VERSION//.}";

echo "Using a Grails version of $GRAILS_VERSION"

source "/home/travis/.sdkman/bin/sdkman-init.sh"
sdk use grails $GRAILS_VERSION

grails clean

if [ $nVersion -lt 240 ]
then
  echo "Using 'grails upgrade' to upgrade application"
  grails upgrade --non-interactive
else
  echo "Using 'set-version' to upgrade application"
  grails set-version $GRAILS_VERSION
fi

grails test-app --unit

grails package-plugin
