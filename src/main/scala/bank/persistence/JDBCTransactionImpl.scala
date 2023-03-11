package bank.persistence

import com.typesafe.config.Config

import scala.util.Try

class JDBCTransactionImpl(config: Config) extends Transaction {
  val datasource = new HikariCPDatasource(config)
  val txn: AnyRef = datasource.getConnection

  override def startTransaction(): Try[Unit] = Try {
    txn match {
      case conn: java.sql.Connection=>
        conn.setAutoCommit(false)
    }
  }
  override def endTransaction(): Try[Unit] = Try {
    txn match {
      case conn: java.sql.Connection=>
        conn.commit()
    }
  }
}
