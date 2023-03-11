package bank

import bank.persistence.{CustomerProfileJDBCDAO, Datasource}

import scala.util.{Failure, Success, Try}

class CustomerProfileJDBCDAOTest(datasource: Datasource) extends CustomerProfileJDBCDAO(datasource) {
  override def truncate(): Unit = {
    Try {
      val conn = datasource.getConnection
      val statement = conn.createStatement()
      statement.executeUpdate(s"""TRUNCATE CUSTOMER_PROFILE;""".stripMargin)
    } match {
      case Success(_)=> ()
      case Failure(exception)=>
        exception.printStackTrace()
    }
  }
}
