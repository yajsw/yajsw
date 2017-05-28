yajsw-stable-12.09

    * Change: do not expose internaly required ports to the network. use localhost loopback. @see: https://sourceforge.net/p/yajsw/bugs/132/
	* Change: tray open console performance.
	* Change: upgrade beanutils. @see https://sourceforge.net/p/yajsw/bugs/131/
	* change: jvm controller: ignore key message if we already received correct key.
	* change: update tp netty-all-4.0.47. @see: https://sourceforge.net/p/yajsw/support-requests/25/
	* change: update to groovy-2.4.11 @see https://sourceforge.net/p/yajsw/support-requests/26/
	* bug: communication between wrapper and app is not logged
	* bug: @see https://sourceforge.net/p/yajsw/discussion/810311/thread/4dad0e51/. Update to jna-4.2.2
	* bug: posix JAVA_HOME bug. @see https://sourceforge.net/p/yajsw/patches/12/
	
NOTE: JNA 4.2.2 has issues with some windows versions, JNA 4.3.0 and JNA 4.4.0 have issues some linux versions.
This release of yajsw includes JNA 4.4.0 but the build for linux-x86-64 is taken from elasticsearch-5.4.0.
If you are having issues with JNA please create a ticket.

https://github.com/java-native-access/jna/issues/771
https://github.com/elastic/elasticsearch/issues/23640
https://unix.stackexchange.com/questions/176489/how-to-update-glibc-to-2-14-in-centos-6-5
https://github.com/java-native-access/jna/issues/636
	