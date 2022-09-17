package bank.persistence

import bank.domain.{AccountStatus, AccountType}

import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

class AccountJDBCDAO(implicit datasource: Datasource) extends AccountDAO {

  private val createTblSql = """
                       |CREATE TABLE IF NOT EXISTS ACCOUNT(
                       |  id                    bigint  NOT NULL  AUTO_INCREMENT PRIMARY KEY,
                       |  fk_customer_profile   bigint,
                       |  account_number        varchar(255) NOT NULL UNIQUE,
                       |  account_type          varchar(20) NOT NULL,
                       |  account_status        varchar(20),
                       |  balance               decimal(12,2),
                       |  FOREIGN KEY fk_customer_profile REFERENCES CUSTOMER_PROFILE(id)
                       |) engine=InnoDB DEFAULT CHARSET=utf8;
                       |""".stripMargin

  datasource.getConnection.createStatement().execute(createTblSql)

  override def saveAccount(customerId: Int, account: WriteAccount)
                          (implicit transaction: Option[Transaction]): Future[Try[Int]] = {
    Future {
      Try {
        val conn = transaction.map(_.txn).map {
          case txn: java.sql.Connection=>
            txn
          case _ => datasource.getConnection
        }.getOrElse(datasource.getConnection)

        val statement = conn.prepareStatement(
          """
            |INSERT INTO ACCOUNT
            |(fk_customer_profile, account_number, account_type, account_status, balance)
            |VALUES (?, ?, ?, ?, ?)
            |""".stripMargin)

        statement.setInt(1, customerId)
        statement.setString(2, account.account_number)
        statement.setString(3, account.account_type)
        account.account_status.foreach(value=> statement.setString(4, value))
        account.balance.foreach(value=> statement.setBigDecimal(5, value.bigDecimal))
        statement.execute()

        val statement2 = conn.createStatement()
        val resultSet = statement2.executeQuery(s"SELECT id FROM ACCOUNT WHERE account_number = ${account.account_number}")
        resultSet.getInt("id")
      }
    }
  }

  override def getAccount(accNum: String): Future[Option[ReadAccount]] = Future {
    val conn = datasource.getConnection
    val statement = conn.prepareStatement(s"SELECT * FROM ACCOUNT WHERE account_number = ?")
    statement.setString(1, accNum)
    val resultSet = statement.executeQuery()
    if (resultSet.first()) {
      val id = resultSet.getInt("id")
      val customerProfileId = Option(resultSet.getInt("fk_customer_profile"))
      val accountNumber = resultSet.getString("account_number")
      val accountType = resultSet.getString("account_type")
      val accountStatus = Option(resultSet.getString("account_status"))
      val balance = Option(resultSet.getBigDecimal("balance"))
        .map(value=> BigDecimal(value))

      Some(
        ReadAccount(
          id,
          customerProfileId,
          accountNumber,
          accountType,
          accountStatus,
          balance
        )
      )
    } else {
      None
    }
  }

}
