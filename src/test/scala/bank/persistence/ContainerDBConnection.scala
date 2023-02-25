package bank.persistence

import com.dimafeng.testcontainers.{
  ForAllTestContainer, MariaDBContainer}
import com.typesafe.config.ConfigFactory
import org.scalatest.Suite

trait ContainerDBConnection extends ForAllTestContainer {
  self: Suite=>

  val container = MariaDBContainer("mariadb:10.3")

  lazy val configOverride = s"""
  banking-app {
     database {
       driver = ${container.driverClassName}
       url = "${container.jdbcUrl}"
       username = ${container.username}
       password = ${container.password}
     }
  }
  """

  lazy val config = ConfigFactory.parseString(configOverride)
    .withFallback(ConfigFactory.load())
}
