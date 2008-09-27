#!/bin/bash
mvn install:install-file -Dfile=target/webtools-1.0-SNAPSHOT.jar  -DgroupId=org.beynet.webtools -DartifactId=webtools -Dversion=1.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true 