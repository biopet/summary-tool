organization := "nl.biopet"
name := "biopet-summary-tool"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.11"

resolvers += Resolver.mavenLocal

libraryDependencies += "nl.biopet" % "biopet-summary_2.11" % "0.1.0-SNAPSHOT"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % Test
libraryDependencies += "org.testng" % "testng" % "6.8" % Test
