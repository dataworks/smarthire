package applicant.etl

import applicant.nlp._
import java.text.DecimalFormat
import java.net.{URL, HttpURLConnection}
import scala.io._
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.mutable.{ListBuffer, Map, LinkedHashMap}

/**
 * Class to retrieve JSON data from URLs and convert them
 * to Scala maps for saving to Elasticsearch
 *
 */
object ApiMapper {
  /**
   * Retrieves data from github api and converts it to a map of [String,String]
   * @param github A string of the parsed applicant's GitHub URL
   * @return A [String,String] map of Github data from the Github api
   */
  def githubAPI(github : String) : Map[String,String] = {
    LinkParser.parseGithubProfile("https://api.github.com/users/", github) match {
      case Some(apiUrl) =>
        val jsonString = scala.io.Source.fromURL(apiUrl).mkString
        val parsedJson = parse(jsonString)
        object VarToString extends CustomSerializer[String](format => (
          {
            case JBool(false) => format.toString
            case JBool(true) => format.toString
          },
          {
            case format: String => JString(format)
          }
        ))
        implicit val formats = DefaultFormats + VarToString

        val gitJsonMap = parsedJson.extract[Map[String, String]]
        println(gitJsonMap)
        return gitJsonMap
      case None =>
        return Map()
    }
  }
}
