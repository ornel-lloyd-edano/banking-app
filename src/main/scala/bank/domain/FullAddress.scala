package bank.domain

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class FullAddress(primary: FullAddress.Address,
                       secondary: Option[FullAddress.Address])

object FullAddress {

  case class Address(
    city: Option[String],
    province: Option[String],
    town: Option[String],
    street: String,
    lot: Option[String],
    block: Option[String],
    buildingName: Option[String],
    apartmentNumber: Option[String],
    isVerified: Boolean
  )

  def fromDAO(address: bank.persistence.Address): Address = {
    Address(
      city = address.city,
      province = address.province,
      town = address.town,
      street = address.street,
      lot = address.lot,
      block = address.block,
      buildingName = address.building_name,
      apartmentNumber = address.apartment_number,
      isVerified = false
    )
  }

  implicit val formatAddress: RootJsonFormat[Address] = jsonFormat9(Address.apply)
  implicit val formatFullAddress: RootJsonFormat[FullAddress] = jsonFormat2(FullAddress.apply)
}