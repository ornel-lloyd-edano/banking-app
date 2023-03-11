package bank.domain

import spray.json.DefaultJsonProtocol._

case class CustomerDocument(filename: String, docType: DocumentType, isVerified: Boolean) {
  def fetchFile: Array[Byte] = ???
}

object CustomerDocument {

  implicit val format = jsonFormat3(CustomerDocument.apply)
}