package bank.domain

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

import scala.util.Try

case class ContactNumber(value: String, isVerified: Boolean) {
  def validate: Try[Unit] = ???
}

object ContactNumber {
  implicit val format: RootJsonFormat[ContactNumber] = jsonFormat2(ContactNumber.apply)
}
