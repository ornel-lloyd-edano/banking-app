package bank.domain

import bank.persistence.{AccountDAO, Address, AddressDAO, CustomerProfileDAO, HikariCPDatasource, ReadAccount, Transaction, JDBCTransactionImpl}
import com.typesafe.config.Config
import spray.json.DefaultJsonProtocol._
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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

  def validateFirstName: Try[Unit] = ???

  def validateMiddleName: Try[Unit] = ???

  def validateLastName: Try[Unit] = ???

  def validateLoginUserName(implicit customerDAO: CustomerProfileDAO, ec: ExecutionContext): Future[Try[Unit]] = {
    customerDAO.getCustomerProfileByUsername(this.loginUserName).map {
      case Some(_)=>
        val isLengthGreaterThanSevenChars = this.loginUserName.length >= 8
        val areAllCharsLetterOrNumber = loginUserName.forall(char=> char.isLetterOrDigit)
        val isFirstCharLetter = loginUserName.charAt(0).isLetter

        (isLengthGreaterThanSevenChars, areAllCharsLetterOrNumber, isFirstCharLetter) match {
          case (false, _, _)=> Failure(new Exception("Login username must be at least 8 characters long"))
          case (_, false, _)=> Failure(new Exception("Login username must be consist of letters and numbers only"))
          case (_, _, false)=> Failure(new Exception("Login username must start with a letter"))
        }

      case None=>
        Failure(new Exception(s"Customer with username: ${this.loginUserName} not found"))
    }
  }

  def validatePassword: Try[Unit] = ???

  def validateEmail: Try[Unit] = this.email.validate

  def validateAccounts: Try[Unit] = ???

  def validate(implicit customerDAO: CustomerProfileDAO, ec: ExecutionContext): Future[Try[Unit]] = {
    validateLoginUserName.map(_.map { _=>
      for {
        _ <- validateFirstName
        _ <- validateMiddleName
        _ <- validateLastName
        _ <- validatePassword
        _ <- validateEmail
        result <- validateAccounts
      } yield result
    })
  }

  def generateAccountNumber(): String = ???

  def openAccount(accountType: AccountType)
                 (implicit customerDAO: CustomerProfileDAO,
                  accountDAO: AccountDAO,
                  config: Config,
                  ec: ExecutionContext):Future[Try[Unit]] = {
    this.validate.flatMap {
      case Success(_)=>
        val customerProfileRow = bank.persistence.CustomerProfile(
          customer_first_name = this.customerFirstName,
          customer_middle_name = this.customerMiddleName,
          customer_last_name = this.customerLastName,
          login_username = this.loginUserName,
          gender = this.gender.toString,
          birth_date = java.sql.Date.valueOf(this.dateOfBirth)
        )

        //C[A].map( A=> B ): C[B]
        //Future[Int].map( Int=> Future[Try[Unit]] ): Future[  Future[Try[Unit]]   ]

        //C[A].flatMap( A => C[B]): C[B]
        //Future[Int].flatMap( Int => Future[Try[Unit]] ): Future[ Try[Unit] ]

        val txn: Option[Transaction] = Some(new JDBCTransactionImpl(config))
        txn.foreach(_.startTransaction())

        txn match {
          case Some(txn)=>

            txn.startTransaction().fold(err=> Future.successful(Failure(err)), _=> {
              customerDAO.saveCustomerProfile(customerProfileRow)(Some(txn)).flatMap { customerId=>

                val account = bank.persistence.WriteAccount(
                  fk_customer_profile = customerId,
                  account_number = generateAccountNumber(),
                  account_type = accountType.toString,
                  account_status = Some(AccountStatus.Pending.toString),
                  balance = None
                )

                accountDAO.saveAccount(customerId, account)(Some(txn))
                  .map(_.fold(ex=> Failure(new RuntimeException(ex.getMessage)),
                    _=> txn.endTransaction()
                  ))
              }
            })

          case None=>
            Future.successful(Failure(new RuntimeException("no avail txn")))
        }

      case fail: Failure[_]=>
        Future.successful(fail)
    }
  }
}

object CustomerProfile {

  def searchByAccountNumber(accNum: String)
                           (implicit customerDAO: CustomerProfileDAO,
                            accountDAO: AccountDAO,
                            addressDAO: AddressDAO,
                            ec: ExecutionContext)
  :Future[Option[Try[CustomerProfile]]] = {

    accountDAO.getAccount(accNum).flatMap {
      case Some(account)=>
        account.fk_customer_profile match {
          case Some(customerId)=>
            customerDAO.getCustomerProfileById(customerId).flatMap {
              case Some(bank.persistence.CustomerProfile( Some(customerId), firstName, midName, lastName, gender, bDate, loginUsr)) =>
                addressDAO.getAddress(customerId).map {
                  case maybeAddresses=>
                    Gender.fromString(gender) match {
                      case Success(gender)=>
                        Some(Success(CustomerProfile(
                          customerFirstName = firstName,
                          customerMiddleName = midName,
                          customerLastName = lastName,
                          loginUserName = loginUsr,
                          password = "",
                          gender = gender,
                          dateOfBirth = bDate.toLocalDate,
                          address = maybeAddresses.find(_.is_primary_address == true).map { primaryAddress=>
                            val secondaryAddress: Option[Address] = Try {
                              maybeAddresses.filterNot(_.is_primary_address == true)
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
                        )))
                      case Failure(ex)=>
                        Some(Failure(ex))
                    }
                }

              case Some(_)=>
                Future.successful(Some(Failure(new IllegalStateException("Primary key is missing"))))
              case None=>
                Future.successful(None)
            }
          case None=>
            Future.successful(None)
        }
      case None=>
        Future.successful(None)
    }
  }


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
