import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.{NotFound, OK, Unauthorized}
import akka.http.scaladsl.server.Directives
import bank.swagger.SwaggerDocService
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

object FakeMobileNumbersValidationService extends Directives with DefaultJsonProtocol with SprayJsonSupport with App {

  val phoneNumberDatabase = Map(
    "Philippines" -> List(
      "+63 813 202 0712",
      "+63 898 251 9340",
      "+63 907 553 0439",
      "+63 817 995 9655",
      "+63 975 307 7385",
      "+63 945 329 8834",
      "+63 909 060 1992",
      "+63 912 313 4340",
      "+63 910 094 0704",
      "+63 947 289 6907"
    ),
    "UAE" -> List(
      "+971 56 669 2083",
      "+971 54 789 2024",
      "+971 54 214 5979",
      "+971 55 778 4985",
      "+971 55 279 6158",
      "+971 52 063 9161",
      "+971 55 543 5188",
      "+971 55 435 0539",
      "+971 54 668 7144",
      "+971 52 522 0761"
    ),
    "US" -> List(
      "+1 252-684-6398",
      "+1 505-646-5099",
      "+1 320-347-3766",
      "+1 262-739-0424",
      "+1 202-910-8736",
      "+1 220-596-6032",
      "+1 605-774-9725",
      "+1 213-642-6999",
      "+1 505-363-6502",
      "+1 505-644-2353"
    ),
    "Japan" -> List(
      "+81 80-2780-6813",
      "+81 80-5000-4967",
      "+81 70-3113-4947",
      "+81 70-1977-3821",
      "+81 90-6454-3290",
      "+81 80-3264-1187",
      "+81 90-4427-8193",
      "+81 80-1358-7535",
      "+81 70-7849-8550",
      "+81 90-3180-9447"
    ),
  )

  def validatePhoneNumber = get {
    path("api" / "phone-validator") {

      parameters("contactNum".as[String], "country".as[String]) { (contactNum, country) =>
        headerValueByName("Authorization") { apiKey=>
          if (apiKey == "123456") {
            /*phoneNumberDatabase.get(country) match {
              case Some(validPhoneNumbers) =>
                //validPhoneNumbers.contains(contactNum)
                validPhoneNumbers.find( item=> item == contactNum ) match {
                  case Some(_) => complete(OK)
                  case None => complete(NotFound, s"Phone $contactNum was not found")
                }
                if (validPhoneNumbers.contains(contactNum)) {
                  complete(OK)
                } else {
                  complete(NotFound, s"Phone $contactNum was not found")
                }

              case None =>
                complete(NotFound, s"Country $country was not found")
            }*/

            (for {
              validPhoneNumbers <- phoneNumberDatabase.get(country)
              validPhoneNumber <- validPhoneNumbers.find( item=> item == contactNum)
            } yield {
              complete(OK, s"Phone $validPhoneNumber was valid")
            }).getOrElse(complete(NotFound, s"Phone $contactNum was not found"))

          } else {
            complete(Unauthorized, "Access Denied")
          }
        }
      }
    }
  }

  implicit val actorSystem = ActorSystem()
  val config = ConfigFactory.load()
  val appHost = config.getString("banking-app.phone-number-service.host")
  val appPort = config.getInt("banking-app.phone-number-service.port")
  import scala.concurrent.ExecutionContext.Implicits.global
  Http().newServerAt(appHost, appPort).bind(this.validatePhoneNumber)
    .foreach(_=> println("Mobile Number Validator Service is Up"))

}
