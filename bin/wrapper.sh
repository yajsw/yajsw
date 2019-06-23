#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# execute a YAJSW command
#
# -----------------------------------------------------------------------------

set -e

version=$("$java_exe" -version 2>&1 | awk -F '"' '/version/ {print $2}')


if [[ "$version" == 1.* ]] 
then
	"$java_exe" "$wrapper_java_options" -Djava.net.preferIPv4Stack=true "$wrapper_java_sys_options" -jar "$wrapper_jar" "$@" 
else
	"$java_exe" "$wrapper_java_options" -Djava.net.preferIPv4Stack=true --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED "$wrapper_java_sys_options" -jar "$wrapper_jar" "$@" 
fi
