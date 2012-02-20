#!/bin/bash

DOMS=http://naiad:7880/fedora
USER=fedoraReadOnlyAdmin
PASS=7HRphHtn
while read pid; do
  curl --user ${USER}:${PASS} ${DOMS}/objects/uuid%3A${pid}/datastreams/SHARD_METADATA/content
done
