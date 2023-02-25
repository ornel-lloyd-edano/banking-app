package bank.controller.dto

import bank.domain.{AccountType, CustomerProfile, FullAddress, Gender}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import java.time.LocalDate

case class RegisterAccount(
                            firstName: String,
                            lastName: String,
                            middleName: Option[String],
                            userName: String,
                            password: String,
                            gender: Gender,
                            dateOfBirth: LocalDate,
                            address: Option[FullAddress],
                            email: String,
                            contactNumber: String,
                            accountType: AccountType
)

object RegisterAccount {
  import CustomerProfile.localDateFormat
  implicit val formatter: RootJsonFormat[RegisterAccount] = jsonFormat11(RegisterAccount.apply)
}