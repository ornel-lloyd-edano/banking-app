package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

sealed trait DocumentType

object DocumentType {
  case object IDPhoto extends DocumentType
  case object Passport extends DocumentType
  case object NationalID extends DocumentType
  case object SSS_ID extends DocumentType
  case object DriversLicense extends DocumentType
  case object TIN_ID extends DocumentType

  implicit val format: RootJsonFormat[DocumentType] = new RootJsonFormat[DocumentType] {
    override def write(obj: DocumentType): JsValue = {
      JsString(obj.getClass.getSimpleName)
    }

    override def read(json: JsValue): DocumentType = {
      json match {
        case JsString(value)=>
          value match {
            case "IDPhoto"=> IDPhoto
            case "Passport"=> Passport
            case "NationalID"=> NationalID
            case "SSS_ID"=> SSS_ID
            case "DriversLicense"=> DriversLicense
            case "TIN_ID"=> TIN_ID
            case other=> throw DeserializationException(s"Document type [$other] is not recognized")
          }
        case _=> throw DeserializationException("Document type must be JsString")
      }
    }
  }
}
