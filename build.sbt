import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.bank"
ThisBuild / organizationName := "bank"

lazy val root = (project in file("."))
  .settings(
    name := "banking-app",
    libraryDependencies ++= List(AkkaStreams, AkkaActors, AkkaHttp, AkkaSpray, SprayJson),
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
