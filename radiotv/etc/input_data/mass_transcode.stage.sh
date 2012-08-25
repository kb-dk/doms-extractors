#!/bin/bash

BES=http://adrasthea:9361/bes_MTC


while read domspid
do
    echo "Queuing transcoding for $domspid"
    echo "Triggering: "$BES"/rest/bes/getobjectstatus?programpid="$domspid
    wget -O - $BES"/rest/bes/getobjectstatus?programpid="$domspid
    sleep 0.1s
done


