package bank.domain

import spray.json.DefaultJsonProtocol._

case class Address(
  city: String,
  province: String,
  town: String,
  street: String,
  lot: Option[String],
  block: Option[String],
  buildingName: Option[String],
  apartmentNumber: Option[String],
  isVerified: Boolean
)

object Address {
  implicit val format = jsonFormat9(Address.apply)
}