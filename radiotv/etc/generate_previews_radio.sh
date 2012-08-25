#!/bin/bash
while read uuid; do
   wget -O /dev/null "http://<preview_tomcat>/bes_OFFLINE/rest/bes/getpreviewstatus?programpid="$uuid
done
