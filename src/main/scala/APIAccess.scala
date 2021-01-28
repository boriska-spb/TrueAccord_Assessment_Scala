import util.{ Failure, Success, Try, Using}
import java.util.Date
import java.text.SimpleDateFormat
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JObject
import Config._

import scala.annotation.tailrec


object APIAccess {
  implicit val formats = DefaultFormats

  // ===== API Data objects ========================

  case class Debt(amount:Float,
                  id:Int)
  {
    override def toString = f"{amount:$amount, id:$id}"
  }

  case class PaymentPlan(amount_to_pay: Float,
                         debt_id: Int,
                         id:Int,
                         installment_amount: Float,
                         installment_frequency: String,
                         start_date : String)
  {
    override def toString: String =
      "{" +
      f"amount_to_pay : $amount_to_pay, " +
      f"debt_id : $debt_id, " +
      f"id : $id," +
      f"installment_amount : $installment_amount, " +
      f"installment_frequency : $installment_frequency, " +
      f"start_date : $start_date" +
      "}"
  }

  case class Payment(amount : Float,
                     date   : String,
                     payment_plan_id : Int)
  {
    override def toString = f"{amount : $amount, date : $date, payment_plan_id : $payment_plan_id}"
  }


  // ===== Conversions ============================================

  def ParseDateOpt(str:String) : Option[Date] = {
    @tailrec
    def _parseHead(formats: List[SimpleDateFormat]): Option[Date] = formats match {
      case Nil => None
      case x :: xs => Try { x.parse(str) } match {
        case Success(date) => Some(date)
        case Failure(err) => _parseHead(formats.tail)
      }
    }

    _parseHead(DateFormats)
  }

  def InstallmentPeriodInDaysOpt(frequency:String):Option[Int] = frequency match {
    case "WEEKLY" => Some(7)
    case "BY_WEEKLY" => Some(14)
    case _ => None
  }

  def FetchDebts(debt_id:Option[Int]=None) : List[Debt] = {
    val param = debt_id.fold("")(i => f"?id = $i")
    Using(io.Source.fromURL(URL_Debts + param)) { src =>
      val json = parse(src.mkString)
      json.extract[List[Debt]]
    } match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
  }

  def FetchPaymentPlans(debt_id:Option[Int]=None) : List[PaymentPlan] = {
    val param = debt_id.fold("")(i => f"?debt_id = $i")
    Using(io.Source.fromURL(URL_PaymentPlans + param)) { src =>
      val json = parse(src.mkString)
      json.extract[List[PaymentPlan]]
    } match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
  }

  def FetchPayments(payment_plan_id:Option[Int]=None) : List[Payment] = {
    val param = payment_plan_id.fold("")(i => f"?payment_plan_id = $i")
    Using(io.Source.fromURL(URL_Payments + param)) { src =>
      val json = parse(src.mkString)
      json.extract[List[Payment]]
    } match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
  }
}
