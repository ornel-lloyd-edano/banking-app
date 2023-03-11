package bank.domain

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class AMLACResponse(first_name: String, last_name: String, status: AMLACStatus)

object AMLACResponse {
  implicit val format: RootJsonFormat[AMLACResponse] =
    jsonFormat3(AMLACResponse.apply)
}
