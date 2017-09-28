organization := "nl.biopet"
name := "biopet-summary-tool"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.11"

resolvers += Resolver.mavenLocal

libraryDependencies += "nl.biopet" %% "biopet-summary" % "0.1.0-SNAPSHOT"
libraryDependencies += "nl.biopet" %% "biopet-tool-utils" % "0.1.0-SNAPSHOT"
libraryDependencies += "nl.biopet" %% "biopet-config-utils" % "0.1.0-SNAPSHOT"

libraryDependencies += "com.h2database" % "h2" % "1.4.196"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % Test
libraryDependencies += "org.testng" % "testng" % "6.8" % Test
