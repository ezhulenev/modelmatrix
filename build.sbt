name in ThisBuild := "Model Matrix"

organization in ThisBuild := "com.collective.modelmatrix"

scalaVersion in ThisBuild := "2.10.5"

scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked"
  //"-Ywarn-unused-import" // turn on whenever you want to discover unused imports, 2.11+
  //"-Xfuture"
  //"-Xlint",
  //"-Ywarn-value-discard"
)

// compiler optimization (2.11+ only)
// still experimental
// scalacOptions       += "-Ybackend:o3"

scalastyleFailOnError in ThisBuild := true

maxErrors in ThisBuild := 5

shellPrompt in ThisBuild := ShellPrompt.buildShellPrompt

libraryDependencies ++= Dependencies.modelMatrix

resolvers in ThisBuild ++= Seq(
  // Typesafe
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeRepo("snapshots"),
  Resolver.typesafeIvyRepo("releases"),
  Resolver.typesafeIvyRepo("snapshots"),
  // Sonatype
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  // Scalaz
  "Scalaz Bintray Repo"  at "http://dl.bintray.com/scalaz/releases",
  // Apache
  "Apache Releases"      at "http://repository.apache.org/content/repositories/releases/",
  "Apache Snapshots"     at "http://repository.apache.org/content/repositories/snapshots",
  // Cloudera
  "Cloudera"             at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)

// Model Matrix project

lazy val modelmatrix = (project in file("."))
  .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
  .configs(IntegrationTest)
  .settings(TestSettings.testSettings:_*)
  .settings(TestSettings.integrationTestSettings:_*)
