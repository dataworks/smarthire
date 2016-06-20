package applicant.etl

import applicant.nlp._
import java.text.DecimalFormat
import java.net.{URL, HttpURLConnection}
import scala.io._
import org.json4s._
import org.json4s.jackson.JsonMethods._

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
            case JBool(false) => "false"
            case JBool(true) => "true"
          },
          {
            case format: String => JString(format)
          }
        ))
        implicit val formats = DefaultFormats + VarToString

        val gitJsonMap = parsedJson.extract[Map[String, String]]
        return gitJsonMap
      case None =>
        return Map()
    }
  }
  /**
   * Given two locations, returns the distance between the two
   * Note: uses the unauthorized Google Maps API internally, may be cut off
   * after too many requests
   * @param location1 First location
   * @param location2 Second location
   * @return Distance between the two locations in meters
   */
  def googlemapsAPI(location1 : String, location2 : String) : Double = {
    val apiUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + location1.trim().replaceAll("\\s", "+") + "&destinations=" + location2.trim().replaceAll("\\s", "+")
    val jsonString = scala.io.Source.fromURL(apiUrl).mkString
    println(jsonString)
    val parsedJson = parse(jsonString)
    implicit val formats = DefaultFormats
    println((((((parsedJson \ "rows")(0) \ "elements")(0))\ "distance") \ "value").extract[Double])// \ "elements")(0) \ "value")
    return 0.0
  }
}
