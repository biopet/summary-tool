organization := "com.github.biopet"
name := "biopet-summary-tool"

scalaVersion := "2.11.11"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.github.biopet" %% "biopet-summary-utils" % "0.1-SNAPSHOT" changing()
libraryDependencies += "com.github.biopet" %% "biopet-tool-utils" % "0.1-SNAPSHOT" changing()
libraryDependencies += "com.github.biopet" %% "biopet-config-utils" % "0.1-SNAPSHOT" changing()

libraryDependencies += "com.github.biopet" %% "biopet-test-utils" % "0.1-SNAPSHOT" % Test changing()

useGpg := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

import ReleaseTransformations._
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
