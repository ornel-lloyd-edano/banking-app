package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

sealed trait Gender

object Gender {
  case object Male extends Gender
  case object Female extends Gender

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