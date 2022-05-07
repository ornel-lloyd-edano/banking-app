package bank

import akka.actor._
import akka.http.scaladsl.Http

object Main extends App {

  println("Welcome to Banking App")
  val controller = new Controller

  implicit val actorSystem = ActorSystem()

  Http().newServerAt("localhost", 9000).bind(controller.allRoutes)

}
