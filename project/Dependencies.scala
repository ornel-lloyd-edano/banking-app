import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.11"

  val AkkaVersion = "2.6.19"
  val AkkaHttpVersion = "10.2.9"

  val AkkaActors = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  val AkkaStreams = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  val AkkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  val AkkaSpray = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion

  val SprayVersion = "1.3.6"
  val SprayJson = "io.spray" %%  "spray-json" % SprayVersion

  val TypeSafeConfig = "com.typesafe" % "config" % "1.4.2"

  val AkkaStreamTest = "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion
  val AkkaHttpTest = "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion

  val Swagger = Seq(
    "jakarta.ws.rs" % "jakarta.ws.rs-api" % "3.0.0",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.7.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2",
    "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % "2.2.0",

    "org.webjars" % "webjars-locator" % "0.45",
    "org.webjars" % "swagger-ui" % "4.6.2"
  )

}
