#!/bin/bash
CLASSPATH=lib/BroadcastExtractionService*.jar
java -cp ${CLASSPATH} -Ddk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir=conf -Dlog4j.configuration=file://${HOME}/bes/conf/log4j.updater.xml dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier.UpdateIdentifierApplication

