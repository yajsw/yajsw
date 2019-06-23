	yajsw-stable-12.14

 
    Tested with java 12 on win 10 and centos 6
    Update to gradle 5.4.1, NOTE: build now requires java 8 for gradle and java 7 for compile
    Upgrade commons: cli 1.4, configurtion2 2.5, io 2.6, lang3 3.9, vfs2 2.3
    Upgrade to latest netty 4.1.36
    Merge commit '4ee1a695981821bf253d70cbc5f566e1161b0bbf'
        Updating static Loggers to use InternalLogger
        Adding Groovy runtime classes with static logging
    Avoid WARNING: An illegal reflective access operation has occurred in groovy. NOTE: workaround until availability of groovy 3.0.0 where this should be resolved.
    Upgrade to latest jna 5.3.1
    Upgrade to latest stable groovy-2.5.7
    Bug: random hard kill on restart
    Merge Request #8: Relativize classpath entries wrt working directory
        NOTE: new configuration property: wrapper.java.app.relativize_classpath


