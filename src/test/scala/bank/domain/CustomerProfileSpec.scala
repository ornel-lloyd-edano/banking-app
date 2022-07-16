package bank.domain

import bank.persistence.{CustomerProfileDAO, CustomerProfileJDBCDAO}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.LocalDate

class CustomerProfileSpec extends AnyWordSpec with Matchers {

  "CustomerProfile username" should {
    implicit val customerDAO: CustomerProfileDAO = new CustomerProfileJDBCDAO
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

}
