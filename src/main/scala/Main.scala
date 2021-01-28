import Config._
import APIAccess._
import net.liftweb.json.DefaultFormats

object Main extends App {

  try {

    if (args.length > 1) {
        Config.Init(args(1))
    }

    // --- Test Config
    println(f"RetryConnection  : ${Config.RetryConnection}")
    println(f"URL_Debts        : ${Config.URL_Debts}")
    println(f"URL_PaymentPlans : ${Config.URL_PaymentPlans}")
    println(f"URL_Payments     : ${Config.URL_Payments}")

    println(f"'2020-09-28T16:18:30Z' -> ${ParseDateOpt("2020-09-28T16:18:30Z").getOrElse("Unrecoginzed date format")}")
    println(f"'2020-09-28'           -> ${ParseDateOpt("2020-09-28").getOrElse("Unrecoginzed date format")}")
    println(f"'2020-09'              -> ${ParseDateOpt("2020-09").getOrElse("Unrecoginzed date format")}")

    // --- Test API
    val debts = APIAccess.FetchDebts(Some(0))
    println(debts)

    val debt_id = if (debts.nonEmpty) Some(debts(0).id) else None
    val plans = APIAccess.FetchPaymentPlans(debt_id)
    println(plans)

    val plan_id = if (plans.nonEmpty) Some(plans(0).id) else None
    val payments = APIAccess.FetchPayments(plan_id)
    println(payments)



  }
  catch {
    case err => println(f"***ERROR*** $err")
  }
}
