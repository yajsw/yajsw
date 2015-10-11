yajsw-beta-12.03

    * Bug: windows service: add quotes to java command if it contains blank
    * Bug: genConfig: error parsing java command line
    * Bug: JVMController: use parameter instead of fixed timeout when reconnecting
    * Change: Log an error if folder listing returns null (in java this may be a network hdd error)
    * Change: update ahessian
    * Change: updated groovy scripts: logging 
    * Change: property: wrapper.posix_spawn is now default for all posix OS
    * Change: MyFileHandler due to license conflict
    * Change: log if a folder listing returns null (java: error accessing a network drive)

NOTE: property: wrapper.posix_spawn is now default for all posix OS