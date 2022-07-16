package bank.persistence

import java.sql.DriverManager
import scala.util.{Failure, Success, Try}

class CustomerProfileJDBCDAO extends CustomerProfileDAO {

  override def getCustomerProfileByUsername(username: String): Option[CustomerProfile] = {
    Try {
      Class.forName("org.mariadb.jdbc.Driver")
        .getDeclaredConstructor().newInstance()
      val conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/banking_app?userUnicode=true&characterEncoding=utf8",
      "root", "root")
      val statement = conn.createStatement()
      val resultSet = statement.executeQuery(s"SELECT * FROM CUSTOMER_PROFILE WHERE login_username = '$username'")
      if (resultSet.first()) {
        val id = resultSet.getLong("id")
        val firstName = resultSet.getString("customer_first_name")
        val middleName = Option(resultSet.getString("customer_middle_name"))
        val lastName = resultSet.getString("customer_last_name")
        Some(CustomerProfile(id, firstName, middleName, lastName, username))
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

  override def saveCustomerProfile(cp: CustomerProfile): Unit = {
    Try {
      Class.forName("org.mariadb.jdbc.Driver")
        .getDeclaredConstructor().newInstance()
      val conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/banking_app?userUnicode=true&characterEncoding=utf8",
        "root", "root")
      val statement = conn.createStatement()
      val result = statement.executeUpdate(
        s"""INSERT INTO CUSTOMER_PROFILE
           |(customer_first_name, customer_middle_name, customer_last_name, login_username)
           |VALUES
           |('${cp.customer_first_name}', ${cp.customer_middle_name.map(mn=>s"'$mn'").getOrElse("NULL")}, '${cp.customer_last_Name}', '${cp.login_username}');""".stripMargin)
      if (result == 1) {
        ()
      } else {
        throw new Exception(s"Failed to insert: ${cp}")
      }
    } match {
      case Success(_)=> ()
      case Failure(exception)=>
        exception.printStackTrace()
    }
  }

  override def truncate(): Unit = {
    Try {
      Class.forName("org.mariadb.jdbc.Driver")
        .getDeclaredConstructor().newInstance()
      val conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/banking_app?userUnicode=true&characterEncoding=utf8",
        "root", "root")
      val statement = conn.createStatement()
      statement.executeUpdate(s"""TRUNCATE CUSTOMER_PROFILE;""".stripMargin)
    } match {
      case Success(_)=> ()
      case Failure(exception)=>
        exception.printStackTrace()
    }
  }
}
