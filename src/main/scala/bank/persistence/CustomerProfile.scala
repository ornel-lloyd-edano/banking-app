package bank.persistence

case class CustomerProfile(
  id: Long,
  customer_first_name: String,
  customer_middle_name: Option[String],
  customer_last_Name: String,
  login_username: String
)
