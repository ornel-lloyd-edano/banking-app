package bank.domain

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpMethods, HttpRequest, HttpResponse, MediaTypes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import bank.controller.dto.RegisterAccount
import bank.domain.exceptions.ValidationException
import bank.persistence.{AccountDAO, Address, AddressDAO, CustomerProfileDAO, HikariCPDatasource, JDBCTransactionImpl, ReadAccount, Transaction}
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.concurrent.duration._

case class CustomerProfile(
  customerFirstName: String,
  customerMiddleName: Option[String],
  customerLastName: String,
  loginUserName: String,
  password: String,
  gender: Gender,
  dateOfBirth: LocalDate,
  address: Option[FullAddress] = None,
  email: Email,
  contactNumber: ContactNumber,
  documents: List[CustomerDocument],
  accounts: List[Account]
) {

  def validateContactNumber(implicit actorSystem: ActorSystem,  mat: Materializer, config: Config, ec: ExecutionContext): Future[Try[Unit]] = {
    println("validating contact number")

    (for {
      host <- Try(config.getString("banking-app.phone-number-service.host"))
      port <- Try(config.getString("banking-app.phone-number-service.port"))
      path <- Try(config.getString("banking-app.phone-number-service.validator-path"))
      apiKey <- Try(config.getString("banking-app.phone-number-service.api-key"))
      hostWithQueryParam = s"http://$host:$port$path?contactNum=${this.contactNumber.value}&country=Philippines"
      getRequest <- Try(HttpRequest(
        uri = Uri(hostWithQueryParam),
        headers = List(RawHeader("Authorization", apiKey)) //JWT later
      ))
    } yield {
      Http().singleRequest(getRequest).flatMap {
        case HttpResponse(code, _, _, _) if code.intValue() >= 200 && code.intValue() < 400 =>
          println("got OK response from mobile number validator service")
          Future.successful(Success( () ))

        case HttpResponse(code, _, content, _) =>
          println(s"status $code got error response from mobile number validator service")
          content.httpEntity.toStrict(2000.milli).map { strict=>
            Failure(new Exception(strict.data.utf8String))
          }
        case other =>
          println(s"received other response" + other)
          Future.successful(Failure(new Exception("Unexpected error")))

      }
    }) match {
      case Success(httpResponse)=> httpResponse
      case Failure(exception) => Future.successful(Failure(exception))
    }
  }

  def validateFirstName: Future[Try[Unit]] =
    Future.successful(Success(()))

  def validateMiddleName: Future[Try[Unit]] = Future.successful(Success(()))

  def validateLastName: Future[Try[Unit]] = Future.successful(Success(()))


  def doesCustomerWithSameUserNameExists(implicit customerDAO: CustomerProfileDAO, ec: ExecutionContext): Future[Boolean] = {
    customerDAO.getCustomerProfileByUsername(this.loginUserName).map(_.nonEmpty)
  }

  def isUserNameAtLeastThisNumberOfChars(numChars: Int): Boolean = {
    this.loginUserName.length >= numChars
  }

  def isUserNameMadeUpOfLetterOrNumberAndLegalSpecialCharacters: Boolean = {
    this.loginUserName.forall(char=> char.isLetterOrDigit || char == '.')
  }

  def isUserNameFirstCharALetter: Boolean = {
    this.loginUserName.charAt(0).isLetter
  }

  def validateLoginUserName(implicit customerDAO: CustomerProfileDAO, ec: ExecutionContext): Future[Try[Unit]] = {
    println("validating login username")
    for {
      _ <- Future.successful {
        if(! isUserNameFirstCharALetter)
          throw new ValidationException(s"Login username [${this.loginUserName}] must start with a letter")
      }

      _ <- doesCustomerWithSameUserNameExists.map { result=>
        if (result)
          throw new ValidationException(s"User with username [${this.loginUserName}] already exists")
      }

      _ <- Future.successful {
        if (! isUserNameAtLeastThisNumberOfChars(8))
          throw new ValidationException(s"Login username [${this.loginUserName}] must be at least 8 characters long")
      }

      _ <- Future.successful {
        if(! isUserNameMadeUpOfLetterOrNumberAndLegalSpecialCharacters)
          throw new ValidationException(s"Login username [${this.loginUserName}] must consist of letters and numbers only")
      }

    } yield Success(()).recover { case ex=>
      ex.printStackTrace()
      Failure(ex)
    }
  }

  def validatePassword: Future[Try[Unit]] = Future.successful(Success(()))

  def validateEmail: Future[Try[Unit]] = Future.successful(Success(()))

  //calling service from AMLAC to check if customer has red flags
  def validateAccounts(implicit actorSystem: ActorSystem,  mat: Materializer, config: Config, ec: ExecutionContext): Future[Try[Unit]] = {
    println("validating account")
    import spray.json._
    (for {
      host <- Try(config.getString("banking-app.amlac-service.host"))
      port <- Try(config.getString("banking-app.amlac-service.port"))
      path <- Try(config.getString("banking-app.amlac-service.validator-path"))
      apiKey <- Try(config.getString("banking-app.amlac-service.api-key"))
      hostWithQueryParam = s"http://$host:$port$path"

      //first name, last name, email, contact number
      val jsonRequest = ByteString(
        s"""
          |{
          |  "first_name": "${this.customerFirstName}",
          |  "last_name": "${this.customerLastName}",
          |  "email": "${this.email.value}",
          |  "contact_number": "${this.contactNumber.value}"
          |}
          |""".stripMargin
      )

      postRequest <- Try(HttpRequest(
        method = HttpMethods.POST,
        uri = Uri(hostWithQueryParam),
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest),
        headers = List(RawHeader("Authorization", apiKey)) //JWT later
      ))
    } yield {
      Http().singleRequest(postRequest).flatMap {
        case HttpResponse(code, _, content, _) if code.intValue() >= 200 && code.intValue() < 400 =>
          content.httpEntity.toStrict(2000.milli).map { strict=>
            Try {
              strict.data.utf8String.parseJson.convertTo[AMLACResponse]
            } match {
              case Success(response) =>
                if (response.status == AMLACStatus.Alert) {
                  Failure(new Exception("Customer has red flag from AMLAC"))
                } else {
                  Success(())
                }
              case Failure(_)=>
                Failure(new Exception("Unable to read/parse response from AMLAC"))
            }


          }

          println("got OK response from AMLAC service")
          Future.successful(Success( () ))

        case HttpResponse(code, _, content, _) =>
          println(s"status $code got error response from AMLAC service")
          content.httpEntity.toStrict(2000.milli).map { strict=>
            val error = strict.data.utf8String.parseJson.convertTo[AMLACErrorResponse]
            Failure(new Exception(error.message))
          }
        case other =>
          println(s"received other response" + other)
          Future.successful(Failure(new Exception("Unexpected error")))

      }
    }) match {
      case Success(httpResponse)=> httpResponse
      case Failure(exception) => Future.successful(Failure(exception))
    }

    ???
  }

  def validate(implicit customerDAO: CustomerProfileDAO, ec: ExecutionContext, actorSystem: ActorSystem, config: Config): Future[Try[Unit]] = {
    println("validating")
    val validUsrName: Future[Try[Unit]] = validateLoginUserName
    val validFName: Future[Try[Unit]] = validateFirstName
    val validMName: Future[Try[Unit]] = validateMiddleName
    val validLName: Future[Try[Unit]] = validateLastName
    val validPwd: Future[Try[Unit]] = validatePassword
    val validEmail: Future[Try[Unit]] = validateEmail
    val validAcc: Future[Try[Unit]] = validateAccounts
    val validPhoneNum: Future[Try[Unit]] = validateContactNumber
    //1 - use cats core and monad transformer to chain nested monads without regard for outer monad
    //2 - convert inner monad's negative result to outer monad's negative
        //ex. Future[Try[Unit]] = Future(Failure(Exception)) -> Future(throw Exception)
    for {
      _ <- validUsrName.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validFName.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validMName.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validLName.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validPwd.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validEmail.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      _ <- validPhoneNum.map {
        case Failure(ex) => throw ex
        case Success(unit) => unit
      }
      result <- validAcc
    } yield result

  }.recover {
    case exception: Exception =>
      exception.printStackTrace()
      Failure(exception)
  }

  def generateAccountNumber(): String = UUID.randomUUID().toString

  def openAccount(accountType: AccountType)
                 (implicit customerDAO: CustomerProfileDAO,
                  accountDAO: AccountDAO,
                  config: Config,
                  actorSystem: ActorSystem,
                  ec: ExecutionContext):Future[Try[Unit]] = {
    println("opening account")
    this.validate.flatMap {
      case Success(_)=>
        println("valid profile")
        val customerProfileRow = bank.persistence.CustomerProfile(
          customer_first_name = this.customerFirstName,
          customer_middle_name = this.customerMiddleName,
          customer_last_name = this.customerLastName,
          login_username = this.loginUserName,
          gender = this.gender.toString,
          birth_date = java.sql.Date.valueOf(this.dateOfBirth)
        )

        val txn: Option[Transaction] = Some(new JDBCTransactionImpl(config))
        txn.foreach(_.startTransaction())

        txn match {
          case Some(txn)=>
            txn.startTransaction().fold(err=> Future.successful(Failure(err)), _=> {
              println("saving profile to database")
              customerDAO.saveCustomerProfile(customerProfileRow)(Some(txn)).flatMap { customerId=>
                println("profile saved to database")
                val account = bank.persistence.WriteAccount(
                  fk_customer_profile = customerId,
                  account_number = generateAccountNumber(),
                  account_type = accountType.toString,
                  account_status = Some(AccountStatus.Pending.toString),
                  balance = None
                )
                println("saving account to database")
                accountDAO.saveAccount(customerId, account)(Some(txn))
                  .map(_.fold(ex=> {
                    println("failed to save account to database")
                    Failure(new RuntimeException(ex.getMessage))
                  },
                    _=> {
                      println("account saved to database")
                      txn.endTransaction()
                    }))
              }
            })

          case None=>
            Future.successful(Failure(new RuntimeException("no avail txn")))
        }

      case fail=>
        println(s"validation failed: $fail")
        Future.successful(fail)
    }
  }
}

object CustomerProfile {

  def toDomain(customerProfile: bank.persistence.CustomerProfile,
               gender: Gender,
               addressList: Seq[Address],
               account: ReadAccount
              ): CustomerProfile = {
    CustomerProfile(
      customerFirstName = customerProfile.customer_first_name,
      customerMiddleName = customerProfile.customer_middle_name,
      customerLastName = customerProfile.customer_last_name,
      loginUserName = customerProfile.login_username,
      password = "",
      gender = gender,
      dateOfBirth = customerProfile.birth_date.toLocalDate,
      address =
        addressList.find(_.is_primary_address == true).map { primaryAddress=>
          val secondaryAddress: Option[Address] = Try {
            addressList.filterNot(_.is_primary_address == true)
              .sortBy(_.created_at).last
          }.toOption

          FullAddress(
            FullAddress.fromDAO(primaryAddress),
            secondaryAddress.map(FullAddress.fromDAO)
          )
      },
      email = null, //TODO read/write 'email' from/to CONTACT_DETAILS table
      contactNumber = null, //TODO read/write 'contactNumber' from/to CONTACT_DETAILS table
      documents = Nil, //TODO read/write 'documents' from/to CUSTOMER_DOCUMENTS table
      accounts = (account.account_status, account.balance) match {
        case (Some(accStatus), Some(balance))=>
          List(
            Account(
              accountNumber = account.account_number,
              accountType = AccountType.fromString(account.account_type),
              status = AccountStatus.fromString(accStatus),
              balance = balance
            )
          )
        case _ => Nil
      }
    )
  }


  def searchByAccountNumber(accNum: String)
                           (implicit customerDAO: CustomerProfileDAO,
                            accountDAO: AccountDAO,
                            addressDAO: AddressDAO,
                            ec: ExecutionContext):Future[Option[Try[CustomerProfile]]] = {
    for {
      account <- accountDAO.getAccount(accNum)
        .map(_.fold[ReadAccount] (throw new Exception("Account not found")) (identity))

      customerId <- account.fk_customer_profile
        .fold[Future[Int]] (Future.failed(new Exception("Has account but empty customer profile foreign key"))) (Future.successful(_))

      customerProfile <- customerDAO.getCustomerProfileById(customerId)
        .map(_.fold[bank.persistence.CustomerProfile] (throw new Exception("Profile not found")) (identity) )

      addressList <- addressDAO.getAddress(customerId)

      gender <- Gender.fromString(customerProfile.gender).fold(ex=> Future.failed(ex), Future.successful(_))
    } yield {
      Option(Try(toDomain(customerProfile, gender, addressList, account)))
    }
  }//end


  implicit val localDateFormat: RootJsonFormat[LocalDate] = new RootJsonFormat[LocalDate] {
    override def read(json: JsValue): LocalDate = {
      json match {
        case JsString(date)=> LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        case _=> throw DeserializationException("Cannot read json string localdate")
      }
    }

    override def write(obj: LocalDate): JsValue = {
      JsString(obj.toString)
    }
  }

  implicit val format: RootJsonFormat[CustomerProfile] = jsonFormat12(CustomerProfile.apply)

}
