import scala.util.{Failure, Success, Try}
import java.util.Date
import java.text.SimpleDateFormat
import util.DynamicVariable
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JObject
import Config._

import scala.annotation.tailrec


object APIAccess {
  implicit val formats = DefaultFormats

  // ===== API Data objects ========================

  case class Debt(val amount:Float,
                  val id:Int)
  {
    override def toString = f"{amount:$amount, id:$id}"
  }

  case class PaymentPlan(val amount_to_pay: Float,
                         val debt_id: Int,
                         val id:Int,
                         val installment_amount: Float,
                         val installment_frequency: String,
                         val start_date : String)
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

  case class Payment(val amount : Float,
                     val date   : String,
                     val payment_plan_id : Int)
  {
    override def toString = f"{amount : $amount, date : $date, payment_plan_id : $payment_plan_id}"
  }


  // ===== Conversions ============================================

  def ParseDateOpt(str:String) : Option[Date] = {
    @tailrec
    def _parse(formats: List[SimpleDateFormat]): Option[Date] = formats match {
      case Nil => None
      case x :: xs => Try { x.parse(str) } match {
        case Success(date) => Some(date)
        case Failure(err) => _parse(formats.tail)
      }
    }

    _parse(DateFormats)
  }

  def InstallmentPeriodInDaysOpt(frequency:String):Option[Int] = frequency match {
    case "WEEKLY" => Some(7)
    case "BY_WEEKLY" => Some(14)
    case f@_ => None
  }

  def FetchDebts(debt_id:Option[Int]=None) : List[Debt] = {
    val param = debt_id match {
      case Some(i) => f"?id = $i"
      case _ => ""
    }

    val json = parse(io.Source.fromURL(URL_Debts + param).mkString)
    json.extract[List[Debt]]
  }

  def FetchPaymentPlans(debt_id:Option[Int]=None) : List[PaymentPlan] = {
    val param = debt_id match {
      case Some(i) => f"?debt_id = $i"
      case _ => ""
    }
    val json = parse(io.Source.fromURL(URL_PaymentPlans + param).mkString)
    json.extract[List[PaymentPlan]]
  }

  def FetchPayments(payment_plan_id:Option[Int]=None) : List[Payment] = {
    val param = payment_plan_id match {
      case Some(i) => f"?payment_plan_id = $i"
      case _ => ""
    }
    val json = parse(io.Source.fromURL(URL_Payments + param).mkString)
    json.extract[List[Payment]]
  }
}
