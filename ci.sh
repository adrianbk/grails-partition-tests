#!/usr/bin/env bash
set -e

nVersion="${GRAILS_VERSION//.}";

echo "Using a Grails version of $GRAILS_VERSION"

sdk use grails $GRAILS_VERSION

grails clean

if [ $nVersion -lt 240 ]
then
  grails upgrade --non-interactive
else
  grails set-version $GRAILS_VERSION
fi

grails test-app --unit

grails package-plugin
