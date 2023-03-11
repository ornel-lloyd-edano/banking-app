package bank.persistence

import scala.concurrent.Future

trait CustomerProfileDAO {
  def getCustomerProfileByUsername(username: String): Future[Option[CustomerProfile]]

  def getCustomerProfileById(id: Int): Future[Option[CustomerProfile]]

  def getCustomerProfileByAccountNumber(accNum: String): Future[Option[CustomerProfile]]

  def saveCustomerProfile(customerProfile: CustomerProfile)
                         (implicit txn: Option[Transaction] = None): Future[Int]

  def truncate(): Unit
}
