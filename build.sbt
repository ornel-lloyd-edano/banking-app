import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.bank"
ThisBuild / organizationName := "bank"

lazy val root = (project in file("."))
  .settings(
    name := "banking-app",
    libraryDependencies ++= List(
      AkkaStreams,
      AkkaActors,
      AkkaHttp,
      AkkaSpray,
      SprayJson,
      TypeSafeConfig,
      MariaDB,
      HikariCP) ++ Swagger,
    libraryDependencies ++= List(
      scalaTest,
      AkkaStreamTest,
      AkkaHttpTest,
      InMemoryDB,
      ScalaTestContainer,
      MariaDBTestContainer
    ).map(_ % Test)
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
coverageEnabled := true
coverageFailOnMinimum := true
coverageMinimumStmtTotal := 90
coverageMinimumBranchTotal := 90
coverageMinimumStmtPerPackage := 90
coverageMinimumBranchPerPackage := 85
coverageMinimumStmtPerFile := 85
coverageMinimumBranchPerFile := 80
