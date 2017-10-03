yajsw-stable-12.10

    * New: wrapper.ping.check_ack property for bidirectional ping check
    * New: wrapper.delay_shutdown property. Delay shutdown to allow scripts, such as emails to terminate.
    * Bug: bad manifest
    * Bug: windows: wrapper.affinity should support up to 64 processors
    * Bug: NPE when executing shell script
    * Bug: Posix: cannot start process if wrapper.java.additional contains blank
    * Bug: Cannot start jboss wildfily since 12.09: groovy-patch: disable java logging.
    * Change: do not show messages "INFO: lib not found" for extended libs.
