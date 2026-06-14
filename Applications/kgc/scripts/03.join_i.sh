#!/bin/zsh

join() {
  curl -X POST "http://localhost:$1/peer/join"
}

INSTANCES=$1
BASE_PORT=9091

for ((i=0; i<INSTANCES; i++)); do
  join $((BASE_PORT + i))
done
