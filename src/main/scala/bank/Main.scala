package bank

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import bank.controller.Controller
import bank.persistence.{AccountJDBCDAO, AddressJDBCDAO, CustomerProfileJDBCDAO, HikariCPDatasource}
import bank.swagger.SwaggerDocService
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App with RouteConcatenation {
  println("Welcome to Banking App")

  implicit val actorSystem = ActorSystem()
  implicit val config = ConfigFactory.load()
  implicit val datasource = new HikariCPDatasource(config)
  implicit val dao = new CustomerProfileJDBCDAO()
  implicit val accDao = new AccountJDBCDAO()
  implicit val addressDAO = new AddressJDBCDAO()

  val controller = new Controller

  val appHost = config.getString("banking-app.host")
  val appPort = config.getInt("banking-app.port")

  Http().newServerAt(appHost, appPort).bind(SwaggerDocService.routes ~ controller.allRoutes)

}
