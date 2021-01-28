import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable.StringBuilder
import APIAccess._
import Main.{reportBasicDebtInfo, reportExtendedDebtInfo}

import scala.collection.mutable

object APITest {//extends App {

  def runReportBasicInfo(debts:List[Debt]) : String = {
    val fmt = "%s %10.3f %3s\r\n"
    val rows = reportBasicDebtInfo(debts)
    val sb = new mutable.StringBuilder("")
    for (row <- rows) sb ++= fmt.format(row:_*)
    sb.toString
  }

  def runReportExtendedInfo(debts:List[Debt]) : String = {
    val fmt = "%s %10.3f %3s %10.3f %s\r\n"
    val rows = reportExtendedDebtInfo(debts)
    val sb = new mutable.StringBuilder("")
    for (row <- rows) sb ++= fmt.format(row:_*)
    sb.toString
  }

  def runAll(debt_id:Option[Int]=None):String = {
    val debts = FetchDebts(debt_id)
    val sb = new mutable.StringBuilder("")
    sb ++= runReportBasicInfo(debts)
    sb ++= runReportExtendedInfo(debts)
    sb.toString
  }

  //println(runAll())

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

  val DebtIsPaidOff: String = """"""
  val NoData_Debts: String = """"""
  val NoData_PaymentPlans: String = """"""
  val NoData_Payments: String = """"""
  val InvalidDebtAmount: String = """"""
  val MultiplePaymentPlans: String = """"""
  val InvalidPaymentPlanStartDate: String = """"""
  val InvalidPaymentPlanInstallmentFrequency: String = """"""
  val InvalidPaymentAmount: String = """"""

}

class Test_RegressionBaseCase extends AnyFunSuite {
  test("APITest.runAll") {
    assert(APITest.runAll(None) === TestData.RegressionBaseCase)
  }
}

/*TODO

class Test_DebtIsPaidOff extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.DebtIsPaidOff)
  }
}

class Test_NoData_Debts extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(None) === TestData.NoData_Debts)
  }
}

class Test_NoData_PaymentPlans extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(None) === TestData.NoData_PaymentPlans)
  }
}

class Test_NoData_Payments extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(None) === TestData.NoData_Payments)
  }
}

class Test_InvalidDebtAmount extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.InvalidDebtAmount)
  }
}

class Test_MultiplePaymentPlans extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.MultiplePaymentPlans)
  }
}
class Test_InvalidPaymentPlanStartDate extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.InvalidPaymentPlanStartDate)
  }
}
class Test_InvalidPaymentPlanInstallmentFrequency extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.InvalidPaymentPlanInstallmentFrequency)
  }
}
class Test_InvalidPaymentAmount extends AnyFunSuite {
test("APITest.runAll") {
    assert(APITest.runAll(Some(0)) === TestData.InvalidPaymentAmount)
  }
}

*/







