#!/bin/bash

java -cp /home/larm/csr/bes/lib/BroadcastExtractionService-1.6.0.jar:/home/larm/tomcat/lib/servlet-api.jar -Dconfig=/home/larm/csr/bes_PROD.xml -Dlog4j.configuration=file://${HOME}/csr/bes/config/log4j.shard_analyser.xml dk.statsbiblioteket.doms.radiotv.extractor.ExtractorApplication a $*
