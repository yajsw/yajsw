yajsw-stable-12.12

    * Bug: Windows Java 9 won't Run via YAJSW: updated jna
    * Bug: UAC elevation doesn't remember setting of wrapper.home: updated setenv.bat
    * New:  Removing symbolic links configured by chkconfig: new configuration properties: chkconfig_start_priority, chkconfig_stop_priority, updated  daemon.vm
    * Update: API is Missing an Interface: updated documenation
    * Bug: wrapper doesn't delete pidfile
    * New: How to rotate and compress logs. new configuration property: wrapper.logfile.compress compress older file when rotating
    * Change: Small fixes/improvements to the Bash scripts. thanks to  deliriumsky for the patch
    * New: Support for systemd: installDaemonD.sh, installDaemonNoPrivD.sh, uninstallDaemonD.sh, thanks to deliriumsky for the patch
