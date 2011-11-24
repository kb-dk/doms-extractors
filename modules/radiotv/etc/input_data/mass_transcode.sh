#!/bin/bash

DOMS=http://alhena:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=fedoraReadOnlyPass

BES_SERVER_1=http://iapetus:9311/bes_DEVEL_EXTRACTION_1
BES_SERVER_2=http://iapetus:9311/bes_DEVEL_EXTRACTION_2
BES_SERVER_3=http://iapetus:9311/bes_DEVEL_EXTRACTION_3
BES_SERVER_4=http://iapetus:9311/bes_DEVEL_EXTRACTION_4

# Transcode programs
METHOD="getobjectstatus"
# Transcode previews
#METHOD="getpreviewstatus"
# Transcode snapshots
#METHOD="getsnapshotstatus"

NEXT_BES_SERVER="${BES_SERVER_1}"
while read domspid
do
	BES="${NEXT_BES_SERVER}"
	if [ ${NEXT_BES_SERVER} == ${BES_SERVER_1} ]; then
		NEXT_BES_SERVER="${BES_SERVER_2}"
	elif [ ${NEXT_BES_SERVER} == ${BES_SERVER_2} ]; then
		NEXT_BES_SERVER="${BES_SERVER_3}"
	elif [ ${NEXT_BES_SERVER} == ${BES_SERVER_3} ]; then
		NEXT_BES_SERVER="${BES_SERVER_4}"
	else
		NEXT_BES_SERVER="${BES_SERVER_1}"
	fi 
    echo "Queuing transcoding for $domspid on server ${BES}"
    #wget -O - "${BES}/rest/bes/${METHOD}?programpid="$domspid
    sleep 0.1s
done


