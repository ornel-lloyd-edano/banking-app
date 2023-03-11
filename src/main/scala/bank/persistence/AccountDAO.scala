package bank.persistence


import scala.concurrent.Future
import scala.util.Try

trait AccountDAO {

  def saveAccount(customerId: Int, account: WriteAccount)
                 (implicit txn: Option[Transaction] = None): Future[Try[Int]]

  def getAccount(accNum: String): Future[Option[ReadAccount]]

}
