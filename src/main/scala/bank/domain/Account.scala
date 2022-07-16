package bank.domain

import spray.json.DefaultJsonProtocol._

import scala.util.Try

case class Account(
  accountType: AccountType,
  currentBalance: Double,
  status: AccountStatus
) {
  def accountNumber: Option[String] = ???
  def validate: Try[Unit] = ???
}

object Account {
  implicit val formatter = jsonFormat3(Account.apply)
}