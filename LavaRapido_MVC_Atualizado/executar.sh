#!/usr/bin/env bash
cd "$(dirname "$0")"
[ -d out ] || ./compilar.sh
java -cp out lavarapido.Main
