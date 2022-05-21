package bank

import spray.json.DefaultJsonProtocol._

case class Account(
  accountNumber: String,
  currentBalance: Double,
  currency: String,
  customerFirstName: String,
  customerLastName: String
)

object Account {
  implicit val formatter = jsonFormat5(Account.apply)
}