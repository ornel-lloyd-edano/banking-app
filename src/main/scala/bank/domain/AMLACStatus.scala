package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.util.{Failure, Success, Try}

sealed trait AMLACStatus

object AMLACStatus {
  case object OK extends AMLACStatus
  case object Warnings extends AMLACStatus
  case object Alert extends AMLACStatus

  def fromString(status: String): Try[AMLACStatus] = {
    status.toLowerCase match {
      case "ok" => Success(OK)
      case "warning" => Success(Warnings)
      case "alert" => Success(Alert)
      case _ => Failure(new IllegalArgumentException(s"AMLAC status [$status] is not valid"))
    }
  }

  implicit val format: RootJsonFormat[AMLACStatus] = new RootJsonFormat[AMLACStatus] {
    override def write(obj: AMLACStatus): JsValue = {
      JsString(obj.getClass.getSimpleName)
    }

    override def read(json: JsValue): AMLACStatus = {
      json match {
        case JsString(value)=>
          fromString(value).fold(_=> throw DeserializationException(s"AMLACStatus [$value] is not recognized"), identity(_))
        case _=> throw DeserializationException("AMLACStatus must be JsString")
      }
    }
  }
}
