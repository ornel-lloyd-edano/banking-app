package bank.persistence

case class ReadAccount(
  id: Int,
  fk_customer_profile: Option[Int],
  account_number: String,
  account_type: String,
  account_status: Option[String],
  balance: Option[BigDecimal]
)
