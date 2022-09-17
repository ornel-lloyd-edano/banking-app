package bank.persistence

import scala.util.Try

trait Transaction {

  val txn: AnyRef

  def startTransaction():Try[Unit]

  def endTransaction():Try[Unit]

}
