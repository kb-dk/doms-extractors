#!/bin/bash

DOMS=http://alhena:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=fedoraReadOnlyPass
BES=http://iapetus:9311/bes_DEVEL_EXTRACTION


while read domspid
do
    echo "Queuing transcoding for $domspid"
    wget -O - $BES"/rest/bes/getobjectstatus?programpid="$domspid
    sleep 0.1s
done


