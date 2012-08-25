#!/bin/bash
#
# Version 1.0
#
# The purpose of this script is to generate a map between program uuid's and
# the required files to transcode the resulting presentation file.
#
# Usage:
# cat input_file_of_uuid.txt | ./genereate_map_of_uuid_to_required_files.sh > map_of_uuid_and_required_files.txt
#
# The resulting file contains comma separated lists. The first column is the
# uuid and the following columns contains the required files.
#
# UUID's are of the format uuid:000f019c-02e3-480e-b922-234e0bdafb4b
#

DOMS=http://naiad:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=7HRphHtn

while read UUID; do
	echo -n "$UUID"
	UUID_POSTFIX=$(echo "$UUID" | grep -o ".\{36\}$")
	DOMS_RETURN_VALUE=$(curl -s --user ${USER}:${PASS} "${DOMS}/risearch?type=tuples&lang=iTQL&flush=true&format=CSV&query=select%20%24url%20%0Afrom%20%3C%23ri%3E%20%0Awhere%20%24object%20%3Cfedora-model%3Alabel%3E%20'http%3A%2F%2Fwww.statsbiblioteket.dk%2Fdoms%2Fshard%2Fuuid%3A${UUID_POSTFIX}'%0Aand%20%24object%20%3Cfedora-model%3AhasModel%3E%20%3Cfedora%3Adoms%3AContentModel_Shard%3E%0Aand%20%24object%20%3Chttp%3A%2F%2Fdoms.statsbiblioteket.dk%2Frelations%2Fdefault%2F0%2F1%2F%23isPartOfCollection%3E%20%3Cfedora%3Adoms%3ARadioTV_Collection%3E%0Aand%20%24object%20%3Cfedora-model%3Astate%3E%20%3Cfedora-model%3AActive%3E%0Aand%20%24object%20%3Chttp%3A%2F%2Fdoms.statsbiblioteket.dk%2Frelations%2Fdefault%2F0%2F1%2F%23consistsOf%3E%20%24file%0Aand%20%24file%20%3Cfedora-model%3AhasModel%3E%20%3Cfedora%3Adoms%3AContentModel_File%3E%0Aand%20%24file%20%3Cfedora-model%3Alabel%3E%20%24url")
	echo "$DOMS_RETURN_VALUE" | sort | grep -o "http://bitfinder.*" | xargs -I {} echo -n ", {}"
	echo
done
