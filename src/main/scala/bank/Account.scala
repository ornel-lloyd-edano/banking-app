package bank

import bank.Account.AccountNumber
import spray.json.DefaultJsonProtocol._


case class Account(
  accountNumber: AccountNumber,
  currentBalance: Double,
  currency: String,
  customerFirstName: String,
  customerLastName: String
)

object Account {
  type AccountNumber = String
  implicit val formatter = jsonFormat5(Account.apply)
}