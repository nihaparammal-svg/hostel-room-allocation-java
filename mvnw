#!/bin/sh
# Maven Wrapper for Unix / Linux / macOS
if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
else
    echo "Maven command not found in PATH. Please install Maven or use your distribution's package manager."
    exit 1
fi
