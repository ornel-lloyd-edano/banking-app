package bank.controller.dto

import bank.domain.{AccountStatus, AccountType}
import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class CustomerAccount(
  fullName: String,
  contactNumber: String,
  accountNumber: String,
  status: AccountStatus,
  accountType: AccountType,
  balance: BigDecimal
)

object CustomerAccount {
  implicit val format: RootJsonFormat[CustomerAccount] = jsonFormat6(CustomerAccount.apply)
}
