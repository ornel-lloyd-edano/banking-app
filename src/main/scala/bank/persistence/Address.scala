package bank.persistence

import java.time.Instant

case class Address(
  id: Int,
  fk_customer_profile: Option[Int] = None,
  city: Option[String],
  province: Option[String],
  town: Option[String],
  street: String,
  lot: Option[String] = None,
  block: Option[String] = None,
  building_name: Option[String] = None,
  apartment_number: Option[String] = None,
  is_primary_address: Boolean,
  created_at: Instant
)
