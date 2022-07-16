package bank.persistence

trait CustomerProfileDAO {
  def getCustomerProfileByUsername(username: String): Option[CustomerProfile]
  def saveCustomerProfile(customerProfile: CustomerProfile): Unit
  def truncate(): Unit
}
