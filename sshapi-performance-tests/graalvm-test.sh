#!/bin/bash

#/usr/lib/jvm/graalvm-ce-java8-20.1.0/bin/java \
. set-graal

native-image  --no-fallback \
			--enable-all-security-services \
			-Dfile.encoding=UTF-8 -classpath \
		 	/home/tanktarta/Documents/Git/sshapi/sshapi-performance-tests/target/classes:/home/tanktarta/Documents/Git/sshapi/sshapi-core/target/classes:/home/tanktarta/Documents/Git/sshapi/sshapi-jsch/target/classes:/home/tanktarta/.m2/repository/com/jcraft/jsch/0.1.55/jsch-0.1.55.jar:/home/tanktarta/.m2/repository/com/jcraft/jzlib/1.1.3/jzlib-1.1.3.jar:/home/tanktarta/Documents/Git/sshapi/sshapi-ganymed/target/classes:/home/tanktarta/.m2/repository/ch/ethz/ganymed/ganymed-ssh2/262/ganymed-ssh2-262.jar:/home/tanktarta/Documents/Git/sshapi/sshapi-trilead/target/classes:/home/tanktarta/.m2/repository/com/trilead/trilead-ssh2/1.0.0-build222/trilead-ssh2-1.0.0-build222.jar:/home/tanktarta/.m2/repository/org/slf4j/slf4j-log4j12/1.7.25/slf4j-log4j12-1.7.25.jar:/home/tanktarta/.m2/repository/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar:/home/tanktarta/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar SCPPutTest