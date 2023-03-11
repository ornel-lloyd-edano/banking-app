package bank.persistence

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import java.sql.Connection
import scala.util.{Failure, Success, Try}

class HikariCPDatasource(val config: Config) extends Datasource {
  private val hikariConf = new HikariConfig

  val driver: String = Try(config.getString("banking-app.database.driver"))
    .getOrElse("org.mariadb.jdbc.Driver")
  hikariConf.setDriverClassName(driver)

  private val dbUrl = Try(config.getString("banking-app.database.url"))
    .getOrElse( "jdbc:mariadb://localhost:3306/banking_app")
  hikariConf.setJdbcUrl(dbUrl)

  private val username = Try(config.getString("banking-app.database.username"))
    .getOrElse("root")
  hikariConf.setUsername(username)

  hikariConf.setPassword(config.getString("banking-app.database.password"))
  hikariConf.addDataSourceProperty( "cachePrepStmts" , "true" )
  hikariConf.addDataSourceProperty( "prepStmtCacheSize" , "250" )

  val datasource = new HikariDataSource(hikariConf)

  override def getConnection: Connection = {
    datasource.getConnection
  }
}
