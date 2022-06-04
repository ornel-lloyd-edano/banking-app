package bank

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.ByteString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._



class ControllerSpec extends AnyWordSpec
  with Matchers with ScalatestRouteTest with DefaultJsonProtocol {

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

    "get account" in {
      Get("/api/accounts/123") ~> controller.getAccountByAccountNum ~> check {
        status shouldEqual StatusCodes.OK
        val expected: JsValue = """
                         |{
                         |"accountNumber":"123",
                         |"currency":"PHP",
                         |"currentBalance":9999.0,
                         |"customerFirstName":"Lloyd",
                         |"customerLastName":"Edano"
                         |}
                         |""".stripMargin.replace("\n","").toJson

        responseAs[String].toJson shouldEqual expected
      }
    }

    "fail to get non existing account" in {
      Get("/api/accounts/9999") ~> controller.getAccountByAccountNum ~> check {
        status shouldEqual StatusCodes.NotFound
        responseAs[String] shouldEqual "\"Account number 9999 was not found\""
      }
    }
  }

}
