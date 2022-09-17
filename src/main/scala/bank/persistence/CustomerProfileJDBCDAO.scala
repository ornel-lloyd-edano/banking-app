package bank.persistence

import java.sql.{Date, DriverManager}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

class CustomerProfileJDBCDAO(implicit datasource: Datasource) extends CustomerProfileDAO {

  private val createTblSql = """
    |CREATE TABLE IF NOT EXISTS CUSTOMER_PROFILE(
    |  id                    bigint  NOT NULL  AUTO_INCREMENT PRIMARY KEY,
    |  customer_first_name   varchar(255) NOT NULL,
    |  customer_middle_name  varchar(255),
    |  customer_last_Name    varchar(255) NOT NULL,
    |  gender                varchar(10) NOT NULL,
    |  birth_date            date NOT NULL,
    |  login_username        varchar(255) NOT NULL UNIQUE
    |) engine=InnoDB DEFAULT CHARSET=utf8;
    |""".stripMargin

  datasource.getConnection.createStatement().execute(createTblSql)

  override def getCustomerProfileByUsername(username: String): Future[Option[CustomerProfile]] = Future {
    Try {
      val conn = datasource.getConnection
      val statement = conn.prepareStatement(s"SELECT * FROM CUSTOMER_PROFILE WHERE login_username = ? ")
      statement.setString(1, username)
      val resultSet = statement.executeQuery()
      if (resultSet.first()) {
        val id = resultSet.getInt("id")
        val firstName = resultSet.getString("customer_first_name")
        val middleName = Option(resultSet.getString("customer_middle_name"))
        val lastName = resultSet.getString("customer_last_name")
        val gender = resultSet.getString("gender")
        val birthDate: Date = resultSet.getDate("birth_date")
        Some(CustomerProfile(
          id = Option(id),
          customer_first_name = firstName,
          customer_middle_name = middleName,
          customer_last_name = lastName,
          login_username = username,
          gender = gender,
          birth_date = birthDate
        ))
      } else {
        None
      }
    } match {
      case Success(value)=> value
      case Failure(exception: Exception)=>
        exception.printStackTrace()
        None
    }
  }

  override def getCustomerProfileByAccountNumber(accNum: String): Future[Option[CustomerProfile]] = Future {
    ???
  }

  override def saveCustomerProfile(cp: CustomerProfile)(implicit transaction: Option[Transaction]): Future[Int] = Future {
    Try {
      val conn = transaction.map(_.txn).map {
        case txn: java.sql.Connection =>
          txn
        case _ => datasource.getConnection
      }.getOrElse( datasource.getConnection )

      val statement = conn.prepareStatement(
        """
          |INSERT INTO CUSTOMER_PROFILE
          |(customer_first_name, customer_middle_name, customer_last_name,
          |login_username, gender, birth_date)
          |VALUES (?, ?, ?, ?, ?, ?)
          |""".stripMargin)

      statement.setString(1, cp.customer_first_name)
      //NOTE: must prove in unit test that not replacing ? in prepared statement means NULL value in the row
      cp.customer_middle_name.foreach { middleName=>
        statement.setString(2, middleName)
      }
      statement.setString(3, cp.customer_last_name)
      statement.setString(4, cp.login_username)
      statement.setString(5, cp.gender)
      statement.setDate(6, cp.birth_date)
      statement.execute()

    } match {
      case Success(_)=> 1
      case Failure(exception)=>
        exception.printStackTrace()
        -1
    }
  }

  override def truncate(): Unit = ???

  override def getCustomerProfileById(id: Int): Future[Option[CustomerProfile]] = Future {
    Try {
      val conn = datasource.getConnection
      val statement = conn.prepareStatement(s"SELECT * FROM CUSTOMER_PROFILE WHERE id = ? ")
      statement.setInt(1, id)
      val resultSet = statement.executeQuery()
      if (resultSet.first()) {
        val id = resultSet.getInt("id")
        val firstName = resultSet.getString("customer_first_name")
        val middleName = Option(resultSet.getString("customer_middle_name"))
        val lastName = resultSet.getString("customer_last_name")
        val username = resultSet.getString("login_username")
        val gender = resultSet.getString("gender")
        val birthDate = resultSet.getDate("birth_date")
        Some(CustomerProfile(
          id = Option(id),
          customer_first_name = firstName,
          customer_middle_name = middleName,
          customer_last_name = lastName,
          login_username = username,
          gender = gender,
          birth_date = birthDate
        ))
      } else {
        None
      }
    } match {
      case Success(value)=> value
      case Failure(exception: Exception)=>
        exception.printStackTrace()
        None
    }
  }
}
