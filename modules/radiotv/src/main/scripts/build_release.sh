#!/bin/bash
## Run from directory containing pom
cd src/main/scripts; ./create_jaxb_bindings.sh; cd ../../..

mvn -Dmaven.test.skip=true clean package

rm -rf bes_release
rm -rf bes

mkdir bes_release
mkdir bes_release/webapps
mkdir bes_release/bin
mkdir bes_release/conf
mkdir bes_release/lib

cp target/BroadcastExtractionService-*.war bes_release/webapps/bes.war

cp -r target/BroadcastExtractionService-*/WEB-INF/lib/* bes_release/lib

cp etc/DEVEL/*DEVEL* bes_release/conf
cp src/main/resources/log4j* bes_release/conf
cp src/main/resources/update_identifier.properties bes_release/conf

cp src/main/scripts/extractor.sh bes_release/bin
cp src/main/scripts/updater.sh bes_release/bin

mv bes_release bes
zip -r bes.zip bes
