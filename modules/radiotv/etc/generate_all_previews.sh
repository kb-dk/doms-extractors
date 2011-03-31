#!/bin/bash

DOMS=http://alhena:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=fedoraReadOnlyPass
BES=http://iapetus:9311/bes_CSR


for result in $(
wget -q --user=$USER --password=$PASS -O - $DOMS'/risearch?type=tuples&lang=iTQL&flush=true&format=CSV&query=select%20%24label%20%20%24url%20from%20%3C%23ri%3E%20where%20%24object%20%3Cfedora-model%3Alabel%3E%20%24label%20and%20%24object%20%3Cfedora-model%3AhasModel%3E%20%3Cfedora%3Adoms%3AContentModel_Shard%3E%20and%20%24object%20%3Cfedora-model%3Astate%3E%20%3Cfedora-model%3AActive%3E%20and%20%24object%20%3Chttp%3A%2F%2Fdoms.statsbiblioteket.dk%2Frelations%2Fdefault%2F0%2F1%2F%23consistsOf%3E%20%24file%20and%20%24file%20%3Cfedora-model%3Alabel%3E%20%24url'
)
do
    IFS=","
    arr=( $result )
    pid=${arr[0]}
    extension=${arr[1]##*.}
    echo "Generating previw for pid $pid, type $extension"
    wget -O - $BES"/rest/bes/getpreviewstatus?programpid="$pid
    if [ $extension != "wav" ]; then
       wget -O - $BES"/rest/bes/getsnapshotstatus?programpid="$pid 
    fi
done


