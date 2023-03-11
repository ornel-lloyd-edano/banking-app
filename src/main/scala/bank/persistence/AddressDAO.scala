package bank.persistence

import scala.concurrent.Future
import scala.util.Try

trait AddressDAO {

  def getAddress(customerId: Int): Future[Seq[Address]]

  def saveAddress(address: WriteAddress): Future[Try[Int]]
}
