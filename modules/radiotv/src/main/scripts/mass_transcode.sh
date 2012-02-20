#!/bin/bash

# Default is dryrun mode
dry_mesg="(dryrun) "
doit="echo $dry_mesg"
transcoders=./transcoders.conf

# Transcode programs
METHOD="getobjectstatus"
# Transcode previews
#METHOD="getpreviewstatus"
# Transcode snapshots
#METHOD="getsnapshotstatus"

def_method=$METHOD
def_transcoders=$transcoders

print_usage()
{
    echo "Usage: $(basename $0) -f <domspids.txt> [-c <transcoders.conf>] [-m <method> [-d]"
    echo
    echo "-f Input file with a list of domspids to process, use - to take input from stdin"
    echo "-c File that contains a list of transcoders to use, it defaults to $def_transcoders"
    echo "-m This is the BES method to invoke, it defaults to $def_method"
    echo "-d 'doit' mode, the default is dryrun"
    echo
    echo "Transcoder servers should be defined in transcoders.conf"
    echo "using one line for each server."
    echo 
    exit 2
}

# Parse options
if [ $# -gt 0 ]; then
    while getopts f:c:m:d opt
    do
	case $opt in
	    f)
		INPUT=$OPTARG
		;;
	    c)
		transcoders=$OPTARG
		;;
	    m)
		# FIXME - verify that we get a legal method?
		METHOD=$OPTARG
		;;
	    d)
		doit=
		dry_mesg=
		;;
	    \?)
		print_usage
		;;
	esac
    done
    shift `expr $OPTIND - 1`
else
    print_usage
fi

# Check that we got a readable input
if [ -z "$INPUT" ]; then
    # Seems -f was not given
    echo "FATAL: You must specify -f"
    echo
    print_usage 
fi
if [ "$INPUT" = "-" ]; then
    INPUT=/dev/stdin
else
    if [ ! -r "$INPUT" ]; then
	echo "FATAL: Could not read file $INPUT"
	echo
	exit 1
    fi
fi

# Get transcoders
if [ -r "$transcoders" -a -s "$transcoders" ]; then
    serverid=0
    while read transcoder
    do
	let serverid="serverid+1"
	bes_server[$serverid]=$transcoder
    done < $transcoders
    servercount=$serverid
else
    echo "FATAL: Could not read $transcoders or file is empty"
    echo
    print_usage
fi

# Read domspids and schedule transcoding in a round-robin mode
serverid=1
while read domspid
do
    BES="${bes_server[$serverid]}"

    echo "${dry_mesg}Queuing transcoding for $domspid on server ${BES}"
    $doit wget -O - "${BES}/rest/bes/${METHOD}?programpid="$domspid
    sleep 0.1s

    # Compute next server
    if [ $serverid -eq $servercount ]; then
	# Start over
	serverid=1
    else
	let serverid="serverid+1"
    fi
done < $INPUT
