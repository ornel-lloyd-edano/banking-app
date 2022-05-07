import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"

  val AkkaVersion = "2.6.8"
  val AkkaHttpVersion = "10.2.9"

  val AkkaActors = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  val AkkaStreams = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  val AkkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  val AkkaSpray = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion

  val SprayVersion = "1.3.6"
  val SprayJson = "io.spray" %%  "spray-json" % SprayVersion
}
