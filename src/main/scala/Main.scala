import java.time.LocalDate
import java.time.temporal.ChronoUnit
import APIAccess._
import Config._


object Main extends App {
  /**
   * Basic Debt Info Report : id, amount, in-payment-plan
   * @param debts : list of debt info (id, amount)
   * @return      : list of reports rows, each containing values for report columns
   */
  def reportBasicDebtInfo(debts:List[Debt]) : List[List[Any]] = {
    debts.foldRight(List[List[Any]]()) { (dbt,acc) =>
      val plan = APIAccess.FetchPaymentPlans(Some(dbt.id))
      val new_acc = List(dbt.id,             // id
        dbt.amount,                          // amount
        if (plan.nonEmpty) "yes" else "no")  // in payment plan
      new_acc :: acc
    }
  }

  /**
   * Extended Debt Info Report : id, amount, in-payment-plan, remaining-amount, next-payment-due-date
   * @param debts : list of debt info (id, amount)
   * @return      : list of reports rows, each containing values for report columns
   */
  def reportExtendedDebtInfo(debts:List[Debt]) : List[List[Any]] = {
    debts.foldRight(List[List[Any]]()) { (dbt, acc) =>
      val plans = APIAccess.FetchPaymentPlans(Some(dbt.id))
      val row = if (plans.isEmpty) {
        // --- no payment plan -------------
        List(dbt.id,          // id
          dbt.amount,         // amount
          "no",               // in payment plan
          dbt.amount,         // remaining amount
          "N/A")              // next payment due date
      }
      else if (plans.length > 1) {
        // --- error : has multiple payment plans
        List(dbt.id,          // id
          dbt.amount,         // amount
          "*multiple plans*", // in payment plan
          dbt.amount,         // remaining amount
          "N/A")              // next payment due date
      }
      else {
        // --- in payment plan ------------
        val plan = plans.head
        val payments = APIAccess.FetchPayments(Some(plan.id))

        val remaining_amt =
          if (payments.isEmpty) dbt.amount
            else {
            def pmt_date(pmt: Payment): LocalDate = APIAccess.ParseDateOpt(pmt.date) match {
              case Some(date) => date
              case _ => throw new Exception(f"*invalid payment date for f$pmt")
            }

            dbt.amount - payments.
                         filter(pmt_date(_).isBefore(Config.Today)).
                         foldLeft(0.0)((acc, pmt) => acc + pmt.amount)
          }

        val next_pmt_due_date = if (remaining_amt == 0) "N/A"
          else {
            APIAccess.ParseDateOpt(plan.start_date) match {
              case Some(start_date) =>
                APIAccess.InstallmentPeriodInDaysOpt(plan.installment_frequency) match {
                  case Some(period) =>
                    val elapsed_days = ChronoUnit.DAYS.between(start_date, Config.Today)
                    val nperiods = if (elapsed_days % period == 0) elapsed_days / period else elapsed_days / period + 1
                    val next_pmt_due = start_date.plusDays(nperiods * period)
                    next_pmt_due.toString
                  case _ => f"*invalid installment frequency '${plan.installment_frequency}'*"
                }
              case _ => f"*invalid start date '${plan.start_date}'*"
            }
          }

          List(dbt.id,         // id
            dbt.amount,        // amount
            "yes",             // in payment plan
            remaining_amt,     // remaining amount
            next_pmt_due_date) // next payment dues date
      }

      // add row to result
      row :: acc
    }
  }

  // =============== MAIN ===================================================
  try {

      if (args.length > 0) {
          Config.Init(args(0))
      }

      // === Set today to current date
      Config.SetToday(LocalDate.now())

      // ==== Basic Debt Info ===============================================
      val debts = APIAccess.FetchDebts()

      val rows = reportBasicDebtInfo(debts)
      println
      println("Id \tAmount    \tIn Payment Plan")
      println("---\t----------\t---------------")
      for (row <- rows) println("%-3s\t%-10.2f\t%-15s".format(row:_*))


      // ======= Extended Debt Info ===========================================
      val rows_ex = reportExtendedDebtInfo(debts)
      println
      println("Id \tAmount    \tIn Payment Plan \tRemaining Amount\tNext Payment Due")
      println("---\t----------\t----------------\t----------------\t----------------")
      for (row <- rows_ex)
        println("%-3s\t%-10.2f\t%-16s\t%-16.2f\t%s".format(row:_*))
  }
  catch {
      case err:Exception => println(f"***ERROR*** $err")
  }
}
