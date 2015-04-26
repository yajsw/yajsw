yajsw-beta-12.01

    Bug: Quotes in java command line
    Bug: multiple bugs in RuntimeJavaMain
    Change: switched from quartz to yacron4j.
    New: Configuration property: wrapper....script.<n>.maxConcInvoc
    New: Support for vfs-dbx (dropbox). thus command files and automatic updates can be done from dropbox.
    New: Configuration property: wrapper.debug.level
    New: Configuration property: wrapper.filter.debug.default
    New: Configuration properties: wrapper.wrapperJar, wrapper.appJar
    Patch: Make the console output fill the window when resized 
    Patch: Startup under AIX 7.1 fails due to StringIndexOutOfBoundsException
    Patch: Fix "No such file or directory" error on Mac OSX with Java 1.7
    Patch: Fix getPid() in MacOSXService
    Patch: MacOSX: Make sure that install script works on vanila machines (when no custom services installed yet and directory is not exists)
    Patch: made sure wrapper works on Mac when installed in directory with spaces
    Patch: MacOsxService does not append configuration parameters to the execution command
