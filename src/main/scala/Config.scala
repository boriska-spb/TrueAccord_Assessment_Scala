import util.DynamicVariable
import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import java.text.SimpleDateFormat
import java.io.FileNotFoundException

/**
 *  Before first-time use, Config object should be initialized with path to config file as:
 *     Config.Init(path-to-config-file)
 */
object Config {

  // --- Values which need reset with new config path
  case class Data(val RetryConnection:Int, val DateFormats:List[String],val URL:Map[String,String])
  private val _data = new DynamicVariable[Data](Data(
    3,
    List("yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-mm-dd"),
    Map(
      "Debts" -> "https://my-json-server.typicode.com/druska/trueaccord-mock-payments-api/debts",
      "PaymentPlans" -> "https://my-json-server.typicode.com/druska/trueaccord-mock-payments-api/payment_plans",
      "Payments" -> "https://my-json-server.typicode.com/druska/trueaccord-mock-payments-api/payments"
    )
  ))

  private val _dateFormats = new DynamicVariable[List[SimpleDateFormat]](
    _data.value.DateFormats.map(fmt => new SimpleDateFormat(fmt))
  )

  // --- Init with config file path
  def Init(path:String): Unit = {
    implicit val formats = DefaultFormats
    try {
      val cfg_file = io.Source.fromFile(path)
      val json = parse(cfg_file.mkString)
      _data.value = json.extract[Data]
      _dateFormats.value = _data.value.DateFormats.map(fmt => new SimpleDateFormat(fmt))
    }
    catch {
      case err: FileNotFoundException => throw new Exception(f"Config file '$path' not found")
    }
  }

  def RetryConnection: Int = _data.value.RetryConnection
  def DateFormats : List[SimpleDateFormat] = _dateFormats.value
  def URL_Debts: String = _data.value.URL("Debts")
  def URL_PaymentPlans: String = _data.value.URL("PaymentPlans")
  def URL_Payments: String = _data.value.URL("Payments")
}
