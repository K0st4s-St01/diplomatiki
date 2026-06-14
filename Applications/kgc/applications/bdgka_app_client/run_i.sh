#!/bin/bash

INSTANCES=$1
BASE_PORT=9091

mkdir "participants_$INSTANCES/"

for ((i=0; i<INSTANCES; i++)); do
    PORT=$((BASE_PORT + i))

    mvn spring-boot:run \
      -Dexec.mainClass="com.icsd16191.bdgka_app_client.BdgkaAppClientApplication" \
      -Dspring-boot.run.arguments="--server.port=${PORT}" \
      > participants_$INSTANCES/app-${PORT}.log 2>&1 &
done
