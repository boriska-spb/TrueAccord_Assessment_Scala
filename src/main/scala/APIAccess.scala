import util.{ Failure, Success, Try, Using}
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import Config._

import scala.annotation.tailrec


object APIAccess {


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

  def ParseDateOpt(str:String) : Option[LocalDate] = {
    @tailrec
    def _parseHead(formats: List[DateTimeFormatter]): Option[LocalDate] = formats match {
      case Nil => None
      case x :: xs => Try { LocalDate.parse(str) } match {
        case Success(date) => Some(date)
        case Failure(err) => _parseHead(formats.tail)
      }
    }

    _parseHead(DateFormats)
  }

  def InstallmentPeriodInDaysOpt(frequency:String):Option[Int] = frequency match {
    case "WEEKLY" => Some(7)
    case "BI_WEEKLY" => Some(14)
    case _ => None
  }

  // ===== Mock API ==============================================
  import util.DynamicVariable
  import MockAPI.MockURL
  private val _mockURL = new DynamicVariable[Option[MockURL]](None)


  // ===== API Requests ==========================================
  implicit val formats:DefaultFormats = DefaultFormats

  /**
   * Common HTTP Request
   * Incorporates mocking fromURL for unit testing
   * NB! Cannot paramterize by record type : library 'lift-json' does not provide manifest for generic List[T]
   * @param url : url string
   * @return    : Json object (JValue)
   */
  def HTTPRequest(url:String) : JValue = {
    val json_string = _mockURL.value match {
      // --- use mock API
      case Some(mock) => mock.fromURL(url)

      // --- use io.Source.fromURL
      case _ => Using(io.Source.fromURL(url)) { src => src.mkString } match {
        case Success(value) => value
        case Failure(exception) => throw exception
      }
    }
    parse(json_string)
  }

  def FetchDebts(debt_id:Option[Int]=None) : List[Debt] = {
    val param = debt_id.fold("")(i => f"?id=$i")
    HTTPRequest(URL_Debts + param).extract[List[Debt]]
  }

  def FetchPaymentPlans(debt_id:Option[Int]=None) : List[PaymentPlan] = {
    val param = debt_id.fold("")(i => f"?debt_id=$i")
    HTTPRequest(URL_PaymentPlans + param).extract[List[PaymentPlan]]
  }

  def FetchPayments(payment_plan_id:Option[Int]=None) : List[Payment] = {
    val param = payment_plan_id.fold("")(i => f"?payment_plan_id=$i")
    HTTPRequest(URL_Payments + param).extract[List[Payment]]
  }
}



