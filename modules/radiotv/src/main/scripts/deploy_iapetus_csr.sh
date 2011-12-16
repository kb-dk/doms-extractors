#!/bin/sh

mvn clean package
scp target/BroadcastExtractionService-*.war larm@iapetus:/home/larm/war/bes_CSR.war
scp etc/DEVEL/log4j.CSR.xml larm@iapetus:/home/larm/services/conf/
scp etc/DEVEL/bes_CSR.xml larm@iapetus:/home/larm/tomcat/conf/Catalina/localhost
scp etc/DEVEL/bes_CSR.xml larm@iapetus:/home/larm/tomcat/conf/Catalina/localhost/bes_CSR_2.xml


wget -O - http://iapetus:9311/bes_CSR/rest/application.wadl | grep "resource path"

## Now copy extractor/updater scripts and log4j to test + make clean directories.

ssh larm@iapetus rm -r bes
ssh larm@iapetus mkdir bes
ssh larm@iapetus mkdir bes/bin
ssh larm@iapetus mkdir bes/config
scp src/main/scripts/extractor.sh larm@iapetus:bes/bin
scp src/main/scripts/updater.sh larm@iapetus:bes/bin
scp src/main/resources/* larm@iapetus:bes/config
scp -r etc/DEVEL/updater larm@iapetus:bes/updater
ssh larm@iapetus chmod 755 bes/bin/*

