package bank.domain

import bank.persistence.{ContainerDBConnection, CustomerProfileDAO, CustomerProfileJDBCDAO, HikariCPDatasource, InMemDBConnection}
import com.dimafeng.testcontainers.{Container, ForAllTestContainer}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CustomerProfileSpec extends AnyWordSpec with Matchers with ScalaFutures
 with ContainerDBConnection {

  override def afterStart(): Unit = {
    val ds = new HikariCPDatasource(config)
    ds.getConnection.createStatement()
      .execute(
        s"""
           |CREATE TABLE CUSTOMER_PROFILE(
           |  id                    bigint  NOT NULL  AUTO_INCREMENT PRIMARY KEY,
           |  customer_first_name   varchar(255) NOT NULL,
           |  customer_middle_name  varchar(255),
           |  customer_last_Name    varchar(255) NOT NULL,
           |  gender                varchar(10) NOT NULL,
           |  birth_date            date NOT NULL,
           |  login_username        varchar(255) NOT NULL UNIQUE
           |) engine=InnoDB DEFAULT CHARSET=utf8;
           |""".stripMargin)

    ds.getConnection.createStatement()
      .execute(
        s"""
           |INSERT INTO CUSTOMER_PROFILE
           |(customer_first_name, customer_middle_name, customer_last_Name, gender, birth_date, login_username)
           |VALUES
           |('LLOYD', 'PEPITO', 'EDANO', 'M', '1980-10-17', 'qwerty123');
           |""".stripMargin)
  }

  "CustomerProfile username" should {
    //implicit val ds = new HikariCPDatasource(config)

    val mockCustomerProfile = CustomerProfile(
      customerFirstName = "",
      customerMiddleName = None,
      customerLastName = "",
      loginUserName = "",
      password = "",
      gender = Gender.Male,
      dateOfBirth = LocalDate.now(),
      address = Some(FullAddress(
        primary = FullAddress.Address(
          city = None,
          province = None,
          town = None,
          street = "",
          lot = None,
          block = None,
          buildingName= None,
          apartmentNumber = None,
          isVerified = false
        ),
        secondary = None
      )),
      email = Email("", false),
      contactNumber = ContactNumber("",  false),
      documents = List(),
      accounts = List()
    )

    "accept username with at least 8 characters" in {
      implicit val ds = new HikariCPDatasource(config)
      implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
      val validUsername = "ornellloyd"
      val mockCustomer = mockCustomerProfile.copy(loginUserName = validUsername)
      val result = mockCustomer.validateLoginUserName
      whenReady(result) { result =>
        result mustBe Success(())
      }
    }

    "reject username if already existing" in {
      implicit val ds = new HikariCPDatasource(config)
      implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
      val invalidUsername = "qwerty123"
      val mockCustomer = mockCustomerProfile.copy(loginUserName = invalidUsername)
      val result: Future[Try[Unit]] = mockCustomer.validateLoginUserName
      whenReady(result) {
        case Failure(result)=>
          result.getMessage mustBe s"User with username [${invalidUsername}] already exists"
      }
    }

    "reject username with less than 8 characters" in {
      implicit val ds = new HikariCPDatasource(config)
      implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
      val lessThan8CharsUsername = "loyd"
      val mockCustomer = mockCustomerProfile.copy(loginUserName = lessThan8CharsUsername)
      val result = mockCustomer.validateLoginUserName
      whenReady(result) {
        case Failure(exception)=>
          exception.getMessage mustBe s"Login username [$lessThan8CharsUsername] must be at least 8 characters long"
      }
    }

    "accept username with alphanumeric characters" in {
      implicit val ds = new HikariCPDatasource(config)
      implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
      val lessThan8CharsUsername = "ornelloyd123"
      val mockCustomer = mockCustomerProfile.copy(loginUserName = lessThan8CharsUsername)
      val result = mockCustomer.validateLoginUserName
      whenReady(result) { result =>
        result mustBe Success(())
      }
    }

    "reject username with non-alphanumeric characters" in {
      implicit val ds = new HikariCPDatasource(config)
      implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
      val userNameWithAmpersand = "ornel@loyd123"
      val mockCustomer = mockCustomerProfile.copy(loginUserName = userNameWithAmpersand)
      val result = mockCustomer.validateLoginUserName
      whenReady(result) {
        case Failure(exception)=>
          exception.getMessage mustBe s"Login username [${userNameWithAmpersand}] must consist of letters and numbers only"
      }
    }

    /*"accept username having letter as first character" ignore {
      val validUsername = "ornelloyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = validUsername)

      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject username not having letter as first character" ignore {
      val invalidUsername = "123ornelloyd"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = invalidUsername)

      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }

    "accept a unique username (not used by existing customers)" ignore {
      val validUsername = "ornelloyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = validUsername)

      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject a duplicate username (already used by existing customers)" ignore {
      val existingUsername = "ornelloyd123"
      if (customerDAO.getCustomerProfileByUsername(existingUsername).isEmpty) {
        val mockCustomerProfileRecord = bank.persistence.CustomerProfile(1, "Ornel", Some("P"), "Edano", existingUsername)
        customerDAO.saveCustomerProfile(mockCustomerProfileRecord)
      }
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = existingUsername)
      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }*/
  }

  "CustomerProfile password" should {
    "accept a password at least 8 characters in length" ignore {


    }

    "reject a password less than 8 characters in length" ignore {


    }

    "accept a password with at least 1 lowercase letter" ignore {


    }

    "reject a password with no lowercase letter" ignore {


    }

    "accept a password with at least 1 uppercase letter" ignore {


    }

    "reject a password with no uppercase letter" ignore {


    }

    "accept a password with at least 1 special character" ignore {


    }

    "reject a password with no special character" ignore {


    }
  }

  "CustomerProfile email" should {
    "accept an email address in local@domain format" ignore {

    }

    "reject an email address not local@domain format" ignore {

    }

    "accept an email address with alphanumeric characters local and domain" ignore {

    }

    "accept an email address with special characters in local only" ignore {

    }

    "reject an email address with unprintable characters" ignore {

    }

    "reject an email address with special characters in domain" ignore {

    }

    "accept an email address with hyphen or dash character '-' in domain as long as it is not the first or last character in domain" ignore {

    }

    "reject an email address with hyphen or dash as first character '-' in domain" ignore {

    }

    "reject an email address with hyphen or dash as last character '-' in domain" ignore {

    }

    "accept an email address with dot '.' character as long as it is not the first or the last character of the local and domain" ignore {

    }

    "reject an email address with dot '.' character as first character of local" ignore {

    }

    "reject an email address with dot '.' character as last character of local" ignore {

    }

    "reject an email address with dot '.' character as first character of domain" ignore {

    }

    "reject an email address with dot '.' character as last character of domain" ignore {

    }
  }
}
