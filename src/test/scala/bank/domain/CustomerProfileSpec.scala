package bank.domain

import bank.persistence.{CustomerProfileDAO, CustomerProfileJDBCDAO}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class CustomerProfileSpec extends AnyWordSpec with Matchers {

  "CustomerProfile username" should {
    implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO(null)
    customerDAO.truncate()

    def getMockCustomerProfile = CustomerProfile(
      customerFirstName = "",
      customerMiddleName = None,
      customerLastName = "",
      loginUserName = "",
      password = "",
      gender = Gender.Male,
      dateOfBirth = LocalDate.now(),
      address = Address(
        city = "",
        province = "",
        town = "",
        street = "",
        lot = None,
        block = None,
        buildingName= None,
        apartmentNumber = None,
        isVerified = false
      ),
      email = Email("", false),
      contactNumber = ContactNumber("",  false),
      documents = List(),
      accounts = List()
    )

    "accept username with at least 8 characters" in {

      val validUsername = "ornelloyd"
      val mockCustomer = getMockCustomerProfile.copy(loginUserName = validUsername)
      println(mockCustomer.validateLoginUserName.get)
      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject username with less than 8 characters" in {

      val invalidUsername = "loyd"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = invalidUsername)

      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }

    "accept username with alphanumeric characters" in {
      val validUsername = "ornelloyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = validUsername)

      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject username with non-alphanumeric characters" in {
      val invalidUsername = "ornel@loyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = invalidUsername)

      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }

    "accept username having letter as first character" in {
      val validUsername = "ornelloyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = validUsername)

      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject username not having letter as first character" in {
      val invalidUsername = "123ornelloyd"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = invalidUsername)

      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }

    "accept a unique username (not used by existing customers)" in {
      val validUsername = "ornelloyd123"
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = validUsername)

      mockCustomer.validateLoginUserName.isSuccess shouldBe true
    }

    "reject a duplicate username (already used by existing customers)" in {
      val existingUsername = "ornelloyd123"
      if (customerDAO.getCustomerProfileByUsername(existingUsername).isEmpty) {
        val mockCustomerProfileRecord = bank.persistence.CustomerProfile(1, "Ornel", Some("P"), "Edano", existingUsername)
        customerDAO.saveCustomerProfile(mockCustomerProfileRecord)
      }
      val mockCustomer: CustomerProfile = getMockCustomerProfile.copy(loginUserName = existingUsername)
      mockCustomer.validateLoginUserName.isFailure shouldBe true
    }
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
    "accept an email address in local@domain format" in {

    }

    "reject an email address not local@domain format" in {

    }

    "accept an email address with alphanumeric characters local and domain" in {

    }

    "accept an email address with special characters in local only" in {

    }

    "reject an email address with unprintable characters" in {

    }

    "reject an email address with special characters in domain" in {

    }

    "accept an email address with hyphen or dash character '-' in domain as long as it is not the first or last character in domain" in {

    }

    "reject an email address with hyphen or dash as first character '-' in domain" in {

    }

    "reject an email address with hyphen or dash as last character '-' in domain" in {

    }

    "accept an email address with dot '.' character as long as it is not the first or the last character of the local and domain" in {

    }

    "reject an email address with dot '.' character as first character of local" in {

    }

    "reject an email address with dot '.' character as last character of local" in {

    }

    "reject an email address with dot '.' character as first character of domain" in {

    }

    "reject an email address with dot '.' character as last character of domain" in {

    }
  }

}
