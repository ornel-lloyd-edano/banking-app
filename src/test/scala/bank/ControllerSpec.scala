package bank

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ControllerSpec extends AnyWordSpec
  with Matchers with ScalatestRouteTest {

  "Banking App" should {
    val controller = new Controller

    "create an account" in {
      val jsonRequest = ByteString(
        s"""
           |{
           |  "accountNumber": "1234",
           |  "currentBalance": 9999.50,
           |  "currency": "PHP",
           |  "customerFirstName": "Lloyd",
           |  "customerLastName": "Edano"
           |}
        """.stripMargin)

      Post("/api/accounts").withEntity(HttpEntity(MediaTypes.`application/json`, jsonRequest)) ~> controller.createAccount ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldEqual "\"Account created\""
      }

    }
  }

}
