name := "MdReport"

version := "2.0"

scalaVersion := "2.11.8"

organization := "org.broadinstitute"

libraryDependencies ++= Seq(
  "org.broadinstitute" %% "mdtypes" % "1.10",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.5",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.11" % "test",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "com.lambdaworks" %% "jacks" % "2.3.3",
  "com.norbitltd" %% "spoiwo" % "1.1.1",
  "org.broadinstitute" %% "jsonutil" % "1.0",
  "com.github.scopt" %% "scopt" % "3.5.0"
)

// Needed to get play iteratees and others
resolvers ++= Seq(
  "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("public")
)