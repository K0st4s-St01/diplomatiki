#!/bin/sh
set -e
../fabric-samples/test-network/network.sh down
../fabric-samples/test-network/network.sh up createChannel -ca
../fabric-samples/test-network/network.sh deployCC -ccn bdgka_chaincode -ccp /home/xator/diplomatiki/Applications/kgc/chaincode_v2/bdgka-chaincode/ -ccv 1.0.0 -ccl go
