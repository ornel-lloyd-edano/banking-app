package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

sealed trait AccountStatus
object AccountStatus {
  case object Pending extends AccountStatus
  case object Active extends AccountStatus
  case object Blocked extends AccountStatus
  case object Closed extends AccountStatus

  def fromString(accountStatus: String): AccountStatus =
    accountStatus match {
      case "Pending"=> Pending
      case "Active"=> Active
      case "Blocked"=> Blocked
      case "Closed"=> Closed
      case other=> throw DeserializationException(s"Account status [$other] is not recognized")
    }

  implicit val format: JsonFormat[AccountStatus] = new RootJsonFormat[AccountStatus] {
    override def write(obj: AccountStatus): JsValue = {
      JsString(obj.getClass.getSimpleName)
    }

    override def read(json: JsValue): AccountStatus = {
      json match {
        case JsString(value)=> fromString(value)
        case _=> throw DeserializationException("Account status must be JsString")
      }
    }
  }
}
