#!/bin/bash
CLASSPATH=${HOME}/services/webapps/bes_CSR/WEB-INF/lib/BroadcastExtractionService*.jar
java -cp ${CLASSPATH} -Ddk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir=bes/config -Dlog4j.config=bes/config/log4.extractor.xml dk.statsbiblioteket.doms.radiotv.updateidentifier.BroadcastExtractor

