package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import scala.util.{Failure, Success, Try}

sealed trait Gender

object Gender {
  case object Male extends Gender
  case object Female extends Gender

  def fromString(gender: String): Try[Gender] = {
    gender.toLowerCase match {
      case "m" | "male" => Success(Male)
      case "f" | "female" => Success(Female)
      case _ => Failure(new IllegalArgumentException(s"Gender [$gender] is not valid"))
    }
  }

  implicit val format: RootJsonFormat[Gender] = new RootJsonFormat[Gender] {
    override def write(obj: Gender): JsValue = {
      JsString(obj.getClass.getSimpleName)
    }

    override def read(json: JsValue): Gender = {
      json match {
        case JsString(value)=>
          value match {
            case "Male"=> Male
            case "Female"=> Female
            case other=> throw DeserializationException(s"Gender [$other] is not recognized")
          }
        case _=> throw DeserializationException("Gender must be JsString")
      }
    }
  }
}