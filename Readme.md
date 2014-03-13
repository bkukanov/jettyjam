# What is this?

This project contains example projects and maven archetypes for creating 
progressively more complex versions of standalone web applications as 
executable jar files using embedded jetty. The following features will be
explored:

* the necessary Maven configuration to generate the executable jar
* adding a simple servlet starting from the executable jar
* JUnit ExternalResource implementation for Jetty
* adding a Guice servlet
* adding support for REST/JSON endpoints with Jersey and Jackson with Guice
* adding support for Vaadin applications
* enabling HTTPS connectors
 
Each addition from simple to complex will be represented with a new Maven
module. Also the latest versions of all the libraries will be used to build
the project. Later on maven archetypes will be created for each kind of project
making it easy to quickly get up and go with a new project of that type.

