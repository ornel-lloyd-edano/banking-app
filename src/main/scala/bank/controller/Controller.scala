package bank.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, Created, InternalServerError, NotFound, OK}
import akka.http.scaladsl.server.{Directives, Route}
import bank.controller.dto.{CustomerAccount, RegisterAccount}
import bank.domain.exceptions.ValidationException
import bank.domain.{ContactNumber, CustomerDocument, CustomerProfile, Email, FullAddress, Gender}
import bank.persistence
import bank.persistence.{AccountDAO, AccountJDBCDAO, AddressDAO, CustomerProfileDAO, CustomerProfileJDBCDAO, Datasource, HikariCPDatasource}
import com.typesafe.config.{Config, ConfigFactory}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs._
import spray.json._

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class Controller(implicit ec: ExecutionContext,
                 config: Config,
                 accountDAO: AccountDAO,
                 customerProfileDAO: CustomerProfileDAO,
                 addressDAO: AddressDAO,
                 actorSystem: ActorSystem) extends Directives with DefaultJsonProtocol with SprayJsonSupport {


  @POST
  @Path("/api/accounts")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Create new customer account",
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[RegisterAccount])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Account created"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def createAccount: Route = post {
    path("api" / "accounts" ) {
      entity(as[RegisterAccount]) { registerAccount=>

        val customerProfile = CustomerProfile(
          customerFirstName = registerAccount.firstName,
          customerMiddleName = registerAccount.middleName,
          customerLastName = registerAccount.lastName,
          loginUserName = registerAccount.userName,
          password = registerAccount.password,
          gender = registerAccount.gender,
          dateOfBirth = registerAccount.dateOfBirth,
          address = registerAccount.address,
          email = Email(registerAccount.email, isVerified = true),
          contactNumber = ContactNumber(registerAccount.contactNumber, isVerified = true),
          documents = List(),
          accounts = List()
        )

        onComplete( customerProfile.openAccount(registerAccount.accountType).map(_.get) ) {
          case Success(_)=>
            complete(Created, "Account created".toJson)
          case Failure(ex: ValidationException)=>
            complete(BadRequest, s"Reason: ${ex.getMessage}".toJson)
          case Failure(ex)=>
            complete(InternalServerError, s"Reason: ${ex.getMessage}".toJson)
        }
      }
    }
  }

  @GET
  @Path("/api/accounts/{accountNumber}")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Find an account by account number",
    description = "Returns a customer's account based on given account number",
    parameters = Array(
      new Parameter(name = "accountNumber", in = ParameterIn.PATH, required = true, description = "account number of customer",
        content = Array(new Content(schema = new Schema(implementation = classOf[String]))))
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Customer's Account",
        content = Array(new Content(schema = new Schema(implementation = classOf[CustomerAccount])))),
      new ApiResponse(responseCode = "400", description = "Invalid account number supplied"),
      new ApiResponse(responseCode = "404", description = "Account not found")
    )
  )
  def getAccountByAccountNum: Route = get {
    path ("api" / "accounts" / Segment) { accountNumber=>

      onComplete( CustomerProfile.searchByAccountNumber(accountNumber)) {
        case Success(Some(Success(account)))=>
          complete(OK, account.toJson)
        case Success(Some(Failure(ex)))=>
          complete(InternalServerError, s"Reason: ${ex.getMessage}".toJson)
        case Success(None)=>
          complete(NotFound,  s"Account number $accountNumber was not found".toJson)
        case Failure(ex)=>
          complete(InternalServerError, s"Reason: ${ex.getMessage}".toJson)
      }
    }
  }

  val allRoutes: Route =
    createAccount ~ getAccountByAccountNum

}
