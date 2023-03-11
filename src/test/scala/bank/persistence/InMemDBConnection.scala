package bank.persistence

import com.typesafe.config.ConfigFactory

trait InMemDBConnection {

  val configOverride = s"""
  banking-app {
     database {
       driver = org.h2.Driver
       url = "jdbc:h2:mem:test-${java.util.UUID.randomUUID()};MODE=MYSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
       username = sa
       password = ""
     }
  }
  """
  val config = ConfigFactory.parseString(configOverride)
    .withFallback(ConfigFactory.load())

  implicit val ds = new HikariCPDatasource(config)

}
