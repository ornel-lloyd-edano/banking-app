package bank.domain

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class AMLACErrorResponse(operation: String, message: String)

object AMLACErrorResponse {
  implicit val format: RootJsonFormat[AMLACErrorResponse] =
    jsonFormat2(AMLACErrorResponse.apply)
}
