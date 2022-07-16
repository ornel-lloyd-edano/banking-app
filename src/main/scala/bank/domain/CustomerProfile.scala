package bank.domain

import bank.persistence.CustomerProfileDAO
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

case class CustomerProfile(
  customerFirstName: String,
  customerMiddleName: Option[String],
  customerLastName: String,
  loginUserName: String,
  password: String,
  gender: Gender,
  dateOfBirth: LocalDate,
  address: Address,
  email: Email,
  contactNumber: ContactNumber,
  documents: List[CustomerDocument],
  accounts: List[Account]
) {

  def validateFirstName: Try[Unit] = ???

  def validateMiddleName: Try[Unit] = ???

  def validateLastName: Try[Unit] = ???

  def validateLoginUserName(implicit customerDAO: CustomerProfileDAO): Try[Unit] = {
    if (this.loginUserName.size >= 8 &&
      loginUserName.forall(char=> char.isLetterOrDigit)
      && loginUserName.charAt(0).isLetter
      && customerDAO.getCustomerProfileByUsername(this.loginUserName).isEmpty)
      Success(())
    else
      Failure(new Exception("Login username must be at least 8 characters long"))
  }

  def validatePassword: Try[Unit] = ???

  def validateEmail: Try[Unit] = this.email.validate

  def validateAccounts: Try[Unit] = ???

  def validate(implicit customerDAO: CustomerProfileDAO): Try[Unit] = {
    for {
      _ <- validateFirstName
      _ <- validateMiddleName
      _ <- validateLastName
      _ <- validateLoginUserName
      _ <- validatePassword
      _ <- validateEmail
      result <- validateAccounts
    } yield result
  }
}

object CustomerProfile {
  implicit val localDateFormat: RootJsonFormat[LocalDate] = ???
  implicit val format = jsonFormat12(CustomerProfile.apply)

}
