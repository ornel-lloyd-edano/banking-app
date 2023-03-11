package bank.domain

import spray.json.DefaultJsonProtocol._

import scala.util.Try

case class Email(value: String, isVerified: Boolean) {
  def validate: Try[Unit] = ???
}

object Email {
  implicit val format = jsonFormat2(Email.apply)
}
