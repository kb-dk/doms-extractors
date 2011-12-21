#!/bin/bash

java -cp /home/larm/services/webapps/bes_CSR/WEB-INF/lib/BroadcastExtractionService-1.4.4.jar:/home/larm/tomcat/lib/servlet-api.jar  -Dlog4j.configuration=file://${HOME}/bes/config/log4j.shard_analyser.xml dk.statsbiblioteket.doms.radiotv.extractor.ExtractorApplication a $*
