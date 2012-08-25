#!/bin/bash

mkdir streamingContentLinks
mkdir streamingContentLinks/radio_top

for X in 0 1 2 3 4 5 6 7 8 9 a b c d e f
do
	mkdir "streamingContentLinks/radio_${X}"
	mkdir "streamingContentLinks/radio_${X}/files"
	pushd streamingContentLinks/radio_top
	ln -s "../radio_${X}/files" "${X}"
	popd
done

ln -s streamingContentLinks/radio_top streamingContent
