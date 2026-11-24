#!/usr/bin/env bash
# Compila o projeto em ./out (requer JDK 17+)
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java')
echo "Compilado em ./out"
