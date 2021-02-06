import java.time.LocalDate
import org.scalatest.funsuite.AnyFunSuite
import Config._
import MockAPI._
import APIAccess._
import Main.{reportBasicDebtInfo, reportExtendedDebtInfo}

import scala.collection.mutable

object APITest {//extends App {

  // --- Set Today to the date of test data
  // otherwise date calculation may produce different results form the test data used vor unit tests
  Config.SetToday(LocalDate.of(2021,1,28))

  def runReportBasicInfo(debts:List[Debt]) : String = {
    try {
      val fmt = "%s %10.3f %3s\r\n"
      val rows = reportBasicDebtInfo(debts)
      val sb = new mutable.StringBuilder("")
      for (row <- rows) sb ++= fmt.format(row: _*)
      sb.toString
    }
    catch {
      case err:Exception => f"***ERROR*** $err"
    }
  }

  def runReportExtendedInfo(debts:List[Debt]) : String = {
    try {
      val fmt = "%s %10.3f %3s %10.3f %s\r\n"
      val rows = reportExtendedDebtInfo(debts)
      val sb = new mutable.StringBuilder("")
      for (row <- rows) sb ++= fmt.format(row: _*)
      sb.toString
    }
    catch {
      case err:Exception => f"***ERROR*** $err"
    }
  }

  def runAll(debt_id:Option[Int]=None):String = {
    try {
      val debts = FetchDebts(debt_id)
      val sb = new mutable.StringBuilder("")
      sb ++= runReportBasicInfo(debts)
      sb ++= runReportExtendedInfo(debts)
      sb.toString
    }
    catch {
      case err:Exception => f"***ERROR*** $err"
    }
  }

  //println(runAll())
  //def AddResponse(url:String, json: String) = MockAPI.AddResponse(url,json)

}

object TestData {
  val RegressionBaseCase: String =
  """0    123.460 yes
    |1    100.000 yes
    |2   4920.340 yes
    |3  12938.000 yes
    |4   9238.020  no
    |0    123.460 yes     20.960 2021-02-01
    |1    100.000 yes     50.000 2021-01-30
    |2   4920.340 yes    607.670 2021-02-10
    |3  12938.000 yes   9247.745 2021-01-30
    |4   9238.020  no   9238.020 N/A
    |""".stripMargin

  val DebtIsPaidOff: String =
  """0    102.500 yes
    |0    102.500 yes      0.000 N/A
    |""".stripMargin

  val NoData_Debts: String = """"""

  val NoData_PaymentPlans: String =
  """0    123.460  no
    |0    123.460  no    123.460 N/A
    |""".stripMargin

  val NoData_Payments: String =
  """0    123.460 yes    123.460 2021-02-01
    |""".stripMargin

  val InvalidDebtAmount: String =
  """***ERROR*** java.lang.Exception: Error parsing JSON response from API 'https://my-json-server.typicode.com/druska/trueaccord-mock-payments-api/debts?id=0' : [{"amount": XXX,"id": 0}]"""

  val MultiplePaymentPlans: String =
  """0    123.460 yes
    |0    123.460 *multiple plans*    123.460 N/A
    |""".stripMargin

  val InvalidPaymentPlanStartDate: String =
  """0    123.460 yes
    |0    123.460 yes     20.960 *invalid start date '2020-09'*
    |""".stripMargin

  val InvalidPaymentPlanInstallmentFrequency: String =
  """0    123.460 yes
    |0    123.460 yes     20.960 *invalid installment frequency 'MONTHLY'*
    |""".stripMargin

  val InvalidPaymentAmount: String =
  """***ERROR*** java.lang.Exception: Error parsing JSON response from API 'https://my-json-server.typicode.com/druska/trueaccord-mock-payments-api/payments?payment_plan_id=0' : [
    |  {
    |    "amount": XXX,
    |    "date": "2020-09-29",
    |    "payment_plan_id": 0
    |  },
    |  {
    |    "amount": 51.25,
    |    "date": "2020-10-29",
    |    "payment_plan_id": 0
    |  }]""".stripMargin

}

class DebtAPI_UnitTest extends AnyFunSuite {

  test("RegressionBaseCase") {
    assert(APITest.runAll(None) === TestData.RegressionBaseCase)
    println("Test RegressionBaseCase PASSED")
  }

  test("DebtIsPaidOff") {
    APIAccess.SetMockURL(mock = true)

    // FIX : for debt_id=0, total payment amount = 102.5
    val url = URL_Debts + "?id=0"
    val json = """[{"amount": 102.5,"id": 0}]"""
    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.DebtIsPaidOff)
    println("Test DebtIsPaidOff PASSED")
  }

  test("NoData_Debts") {
    APIAccess.SetMockURL(mock = true)

    // FIX : empty list for Debts API
    val url = URL_Debts
    val json = """[]"""
    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(None)
    MockAPI.ClearAllResponses()
    assert(out === TestData.NoData_Debts)
    println("Test NoData_Debts PASSED")
  }

  test("NoData_PaymentPlans") {
    APIAccess.SetMockURL(mock = true)

    // FIX : empty list for PaymentPlans API
    val url = URL_PaymentPlans + "?debt_id=0"
    val json = """[]"""
    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.NoData_PaymentPlans)
    println("Test NoData_PaymentPlans PASSED")
  }

  test("NoData_Payments") {
    APIAccess.SetMockURL(mock = true)

    // FIX Payments API: empty list for payment_plan_id=0 (=> debt_id = 0)
    val url = URL_Payments + "?payment_plan_id=0"
    val json = """[]"""
    MockAPI.AddResponse(url, json)
    val debts = FetchDebts(Some(0))
    val out = APITest.runReportExtendedInfo(debts)
    MockAPI.ClearAllResponses()
    assert(out === TestData.NoData_Payments)
    println("Test NoData_Payments PASSED")
  }

  test("InvalidDebtAmount") {
    APIAccess.SetMockURL(mock = true)

    // FIX : set invalid debt amount for debt_id=0
    val url = URL_Debts + "?id=0"
    val json = """[{"amount": XXX,"id": 0}]"""
    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.InvalidDebtAmount)
    println("Test InvalidDebtAmount PASSED")
  }

  test("MultiplePaymentPlans") {
    APIAccess.SetMockURL(mock = true)

    // FIX PaymentPlans API : multiple payment plans for debt id = 0
    val url = URL_PaymentPlans + "?debt_id=0"
    val json =
    """[{
    |    "amount_to_pay": 102.5,
    |    "debt_id": 0,
    |    "id": 0,
    |    "installment_amount": 51.25,
    |    "installment_frequency": "WEEKLY",
    |    "start_date": "2020-09-28"
    |  },
    |  {
    |    "amount_to_pay": 100,
    |    "debt_id": 0,
    |    "id": 1,
    |    "installment_amount": 25,
    |    "installment_frequency": "WEEKLY",
    |    "start_date": "2020-08-01"
    |  }]""".stripMargin

    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.MultiplePaymentPlans)
    println("Test MultiplePaymentPlans PASSED")
  }

  test("InvalidPaymentPlanStartDate") {
    APIAccess.SetMockURL(mock = true)

    // FIX PaymentPlans : set invalid start date amount for debt_id=0
    val url = URL_PaymentPlans + "?debt_id=0"
    val json =
    """[{
    |    "amount_to_pay": 102.5,
    |    "debt_id": 0,
    |    "id": 0,
    |    "installment_amount": 51.25,
    |    "installment_frequency": "WEEKLY",
    |    "start_date": "2020-09"
    |  }]""".stripMargin

    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.InvalidPaymentPlanStartDate)
    println("Test InvalidPaymentPlanStartDate PASSED")
  }

  test("InvalidPaymentPlanInstallmentFrequency") {
    APIAccess.SetMockURL(mock = true)

    // FIX PaymentPlans : set invalid installment frequency for debt_id=0
    val url = URL_PaymentPlans + "?debt_id=0"
    val json =
    """[{
    |    "amount_to_pay": 102.5,
    |    "debt_id": 0,
    |    "id": 0,
    |    "installment_amount": 51.25,
    |    "installment_frequency": "MONTHLY",
    |    "start_date": "2020-09-28"
    |  }]""".stripMargin

    MockAPI.AddResponse(url, json)
    val out = APITest.runAll(Some(0))
    MockAPI.ClearAllResponses()
    assert(out === TestData.InvalidPaymentPlanInstallmentFrequency)
    println("Test InvalidPaymentPlanInstallmentFrequency PASSED")
  }

  test("InvalidPaymentAmount") {
    APIAccess.SetMockURL(mock = true)

    // FIX Payments : set invalid payment amount for payment_plan_id = 0 ( => debt_id=0)
    val url = URL_Payments + "?payment_plan_id=0"
    val json =
    """[
    |  {
    |    "amount": XXX,
    |    "date": "2020-09-29",
    |    "payment_plan_id": 0
    |  },
    |  {
    |    "amount": 51.25,
    |    "date": "2020-10-29",
    |    "payment_plan_id": 0
    |  }]""".stripMargin

    MockAPI.AddResponse(url, json)
    val debts = FetchDebts(Some(0))
    val out = APITest.runReportExtendedInfo(debts)
    MockAPI.ClearAllResponses()
    assert(out === TestData.InvalidPaymentAmount)
    println("Test InvalidPaymentAmount PASSED")
  }
}








