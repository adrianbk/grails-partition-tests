#!/usr/bin/env bash
set -e

nVersion="${GRAILS_VERSION//.}";

array=(${GRAILS_VERSION//./ })
major=${array[0]}
minor=${array[1]}
echo "Major:${major} Minor:${minor}"

echo "Using a Grails version of $GRAILS_VERSION"
source "/home/travis/.sdkman/bin/sdkman-init.sh"
sdk use grails $GRAILS_VERSION


cd "grails-partition-tests"
grails clean

if (( $major <= 2 && $minor < 4 ))
then
  echo "Using 'grails upgrade' to upgrade application"
  grails upgrade --non-interactive
else
  echo "Using 'set-version' to upgrade application"
  grails set-version $GRAILS_VERSION
  grails set-grails-version $GRAILS_VERSION
fi

grails test-app --unit
grails package-plugin


cd "../app"
if (( $major <= 2 && $minor < 4 ))
then
  echo "Using 'grails upgrade' to upgrade application"
  grails upgrade --non-interactive
else
  echo "Using 'set-version' to upgrade application"
  grails set-version $GRAILS_VERSION
  grails set-grails-version $GRAILS_VERSION
fi

grails test partition-test \"--split=1\" \"--totalSplits=2\"
grails test partition-test \"--split=12\" \"--totalSplits=2\"
