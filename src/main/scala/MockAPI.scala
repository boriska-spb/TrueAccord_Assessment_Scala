import scala.collection.mutable.{Map => MutableMap}

object MockAPI {

  /**
   * Mocked URL responses : map(url -> json-string)
   */
  private val _responses:MutableMap[String,String] = MutableMap()

  /**
   *  Mocked io.source.fromURL : url => json-string
   *  For simplicity, we return String rather than BufferedIterator over Char (as in io.source.fromURL)
   */
  val fromURL : String => String = (url:String) => _responses(url)

  def AddResponse(url:String, json:String) : Unit = _responses(url) = json
  def ClearResponse(url:String) : Unit = _responses.remove(url)
  def ClearAllResponses():Unit = _responses.clear()
}
