package bank.persistence

case class WriteAccount(
  fk_customer_profile: Int,
  account_number: String,
  account_type: String,
  account_status: Option[String],
  balance: Option[BigDecimal]
)
