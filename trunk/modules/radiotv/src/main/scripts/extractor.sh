#!/bin/bash
CLASSPATH=lib/BroadcastExtractionService*.jar
java -cp ${CLASSPATH} -Ddk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir=conf -Dlog4j.configuration=file://${HOME}/bes/conf/log4j.extractor.xml dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.BroadcastExtractor

