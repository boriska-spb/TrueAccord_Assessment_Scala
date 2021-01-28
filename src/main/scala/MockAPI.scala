import scala.collection.mutable.{Map => MutableMap}
import APIAccess._

object MockAPI {

  /**
   * Mocked URL responses : map(url -> json-string)
   */
  private val _responses:MutableMap[String,String] = MutableMap()

  /**
   *  Mocked io.source.fromURL : url => json-string
   *  If url not found in reponses, return None
   *  For simplicity, we return String rather than BufferedIterator (as in io.source.fromURL)
   */
  val fromURL : String => Option[String] = (url:String) => _responses.get(url)

  def AddResponse(url:String, json:String) : Unit = _responses(url) = json
  def ClearResponse(url:String) : Unit = _responses.remove(url)
  def ClearAllResponses():Unit = _responses.clear()

}
