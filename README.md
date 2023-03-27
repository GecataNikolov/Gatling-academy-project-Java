Gatling plugin for Maven - Java. Gatling academy project
============================================

A simple showcase of a Maven project using the Gatling plugin for Maven. Refer to the plugin documentation
[on the Gatling website](https://gatling.io/docs/current/extensions/maven_plugin/) for usage.


It includes:

* [Maven Wrapper](https://maven.apache.org/wrapper/), so that you can immediately run Maven with `./mvnw` without having
  to install it on your computer
* latest version of `io.gatling:gatling-maven-plugin` applied

gatling-maven-plugin-demo
=========================


To execute tests, simply execute the following command:

    ./mvnw gatling:test -Dgatling.simulationClass=<class-with-simulations>

or simply:

    ./mvnw gatling:test
