#!/bin/sh

mvn clean package
scp target/BroadcastExtractionService-1.4.2.war larm@iapetus:/home/larm/heb/bes/deploy_folder/war/bes_HEB.war
scp etc/DEVEL/log4j.HEB.xml larm@iapetus:/home/larm/services/conf/
scp etc/DEVEL/bes_HEB.xml larm@iapetus:/home/larm/tomcat/conf/Catalina/localhost

curl http://iapetus:9311/bes_HEB/rest/application.wadl | grep "resource path"

echo "Test it using:"
echo " - http://iapetus:9311/bes_HEB/rest/application.wadl"
echo " - http://iapetus:9311/bes_HEB/rest/bes/getobjectstatus?programpid=uuid:7421d689-b02c-4456-85dd-2f15ded890f7&title=title&channel=channel&date=date"

echo "Triggering exception..."
curl "http://iapetus:9311/bes_HEB/rest/bes/getobjectstatus?programpid=uuid:7421d689-b02c-4456-85dd-2f15ded890f7&title=title&channel=channel&datedate"
