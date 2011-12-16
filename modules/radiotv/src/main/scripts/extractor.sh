#!/bin/bash
CLASSPATH=${HOME}/services/webapps/bes_CSR/WEB-INF/lib/BroadcastExtractionService*.jar
java -cp ${CLASSPATH} -Ddk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir=${HOME}/bes/config -Dlog4j.configuration=file://${HOME}/bes/config/log4j.extractor.xml dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor

