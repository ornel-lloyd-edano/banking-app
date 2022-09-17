package bank.persistence

case class WriteAddress(
   fk_customer_profile: Int,
   city: Option[String],
   province: Option[String],
   town: Option[String],
   street: String,
   lot: Option[String] = None,
   block: Option[String] = None,
   building_name: Option[String] = None,
   apartment_number: Option[String] = None,
   is_primary_address: Boolean
)
