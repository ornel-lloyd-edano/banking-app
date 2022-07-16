package bank.domain

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

sealed trait AccountType {
  val minRequiredBalance: Double
}

object AccountType {
  case object Savings extends AccountType {
    override val minRequiredBalance: Double = 5000.00
  }
  case object Current extends AccountType {
    override val minRequiredBalance: Double = 0.00
  }
  case object Dollar extends AccountType {
    override val minRequiredBalance: Double = 100
  }
  case object TimeDeposit extends AccountType {
    override val minRequiredBalance: Double = 100000
  }

  implicit val format: RootJsonFormat[AccountType] = new RootJsonFormat[AccountType] {
    override def write(obj: AccountType): JsValue = {
      JsString(obj.getClass.getSimpleName)
    }

    override def read(json: JsValue): AccountType = {
      json match {
        case JsString(value)=>
          value match {
            case "Savings"=> Savings
            case "Current"=> Current
            case "Dollar"=> Dollar
            case "TimeDeposit"=> TimeDeposit
            case other=> throw DeserializationException(s"Account type [$other] is not recognized")
          }
        case _=> throw DeserializationException("Account type must be JsString")
      }
    }
  }
}
