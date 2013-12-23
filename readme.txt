yajsw-stable-11.09

    * New: Property: wrapper.runtime.pidfile: create a pid file for runtime applications
    * New: Property: wrapper.image.javawrapper: wrap a runtime application with a java application, so that (on winodws) we can restart the wrapper process and reconnect to the secondary java wrapper without restarting the native application.
    * New: Property wrapper.filter.debug.<trigger key>=true to enable logging of trigger executions
    * New: Property: wrapper.debug.comm: enable tcp/ip communication trace with wrapped application
    * New: Property: wrapper.ntservice.stop_dependency: Linux stop dependency
    * New: source on sourceforge git
    * Change: src folder structure to meet maven requirements. Gradle build scripts adapted accordingly.
    * Change: monitor.gc when wrapper.java.monitor.gc message template is not set, but wrapper.java.monitor.gc.restart is set in this case no gc information is logged to output, but gc data is sent to the controller.
    * Change: app shutdown script: be invoked only once; avoid triggering of shutdown hook when executing app shutdown script
      assume that app shutdown script will stop the process. if not it will be killed after timeout
    * Change: ahessian: (not relevant for yajsw): new: inverse server rpc.
    * Bug: Searching for 32-bit JVMs on Windows x64
    * Bug: error in mbean objectname when title includes ":"
    * Bug: abs path wrapper.java.classpath with * in linux not working
    * Bug: Linux service scripts not compatible with chkconfig
    * Bug: "Bad substitution" because of not escaped quotes
    * Bug: CyclicBufferFileInputStream: probable endless loop
    * Bug: Runtime process does not restart when invoked through a trigger
