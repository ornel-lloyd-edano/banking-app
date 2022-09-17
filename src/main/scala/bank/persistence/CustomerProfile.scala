package bank.persistence

import java.sql.Date

case class CustomerProfile(
  id: Option[Int] = None,
  customer_first_name: String,
  customer_middle_name: Option[String] = None,
  customer_last_name: String,
  gender: String,
  birth_date: Date,
  login_username: String
)