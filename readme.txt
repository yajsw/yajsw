yajsw-beta-12.04

    * Bug: windows service: bad quotes in java service command line.
    * Bug: script not executed: too many concurrent executions
    * Bug: posix_spawn: application does not set working dir.
    * Bug: Windows 10: Exception in service install
    * Bug: query daemon must be executed with root priv
    * Bug: process does not start if space char in folder or environment
    * Bug: Mac OS X Yosemite: service install fails
    * Bug: 'java -jar wrapper.jar -c http://../wrapper.conf' throws class not found exception
    * Bug: error writing from system tray icon to stdin of wrapped process
    * Bug: some pdh should return long instead of int
    * New: support app shutdown listener, similar to JSW
    * New: keystore function. See documentation for details.
    * Change: update to netty 4.0.33 and jna 4.2.1
    * Change: fix APL license header, format source, organize imports

NOTE: Windows: If you have installed java 7 and 8 on the same machine: set the configuration properties 
				wrapper.java.command = <full path to java>
				wrapper.ntservice.java.command = <full path to java>
