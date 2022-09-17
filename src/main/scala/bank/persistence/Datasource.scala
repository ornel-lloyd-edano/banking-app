package bank.persistence

import java.sql.Connection
import com.typesafe.config.Config

trait Datasource {
  val config: Config
  def getConnection:Connection
}
