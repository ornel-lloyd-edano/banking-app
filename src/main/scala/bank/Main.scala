package bank

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import bank.controller.Controller
import bank.swagger.SwaggerDocService
import com.typesafe.config.ConfigFactory


object Main extends App with RouteConcatenation {

  println("Welcome to Banking App")
  val controller = new Controller

  implicit val actorSystem = ActorSystem()

  val conf = ConfigFactory.load()
  val appHost = conf.getString("banking-app.host")
  val appPort = conf.getInt("banking-app.port")

  Http().newServerAt(appHost, appPort).bind(SwaggerDocService.routes ~ controller.allRoutes)

}
