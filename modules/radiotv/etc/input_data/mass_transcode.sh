#!/bin/bash

DOMS=http://alhena:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=fedoraReadOnlyPass
BES=http://iapetus:9311/bes_DEVEL_EXTRACTION
#BES=http://iapetus:9311/bes_DEVEL_OFFLINE

# Transcode programs
METHOD="getobjectstatus"
# Transcode previews
#METHOD="getpreviewstatus"
# Transcode snapshots
#METHOD="getsnapshotstatus"

while read domspid
do
    echo "Queuing transcoding for $domspid"
    wget -O - "${BES}/rest/bes/${METHOD}?programpid="$domspid
    sleep 0.1s
done


