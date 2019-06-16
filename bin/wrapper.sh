#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# execute a YAJSW command
#
# -----------------------------------------------------------------------------

set -e

"$java_exe" "$wrapper_java_options" -Djava.net.preferIPv4Stack=true --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED "$wrapper_java_sys_options" -jar "$wrapper_jar" "$@"