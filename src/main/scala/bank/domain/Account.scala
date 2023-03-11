package bank.domain

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Account(
  accountNumber: String,
  accountType: AccountType,
  status: AccountStatus,
  balance: BigDecimal
)

object Account {
  implicit val format: RootJsonFormat[Account] = jsonFormat4(Account.apply)
}
