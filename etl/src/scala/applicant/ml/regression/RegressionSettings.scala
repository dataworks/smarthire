package applicant.ml.regression

import scala.collection.mutable.{ListBuffer, Map}
import scala.collection.JavaConversions._

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

object RegressionSettings {

  /**
   * Will check if a boolean is None and
   */
  private def getBool(value: AnyRef): Boolean = {
    if (value == None) {
      return true
    }
    return value.asInstanceOf[Boolean]
  }

  /**
   * Will check if a list option is Some or None and set a list pointer accordingly
   */
  private def getList(value: AnyRef): ListBuffer[String] = {
    if (value == None) {
      return new ListBuffer[String]()
    }
    return value.asInstanceOf[JListWrapper[String]].toList.to[ListBuffer]
  }

  /**
   * Will check if a list of pair of string and list is None and convert it accordingly
   */
  private def getPairList(value: AnyRef): ListBuffer[(String, ListBuffer[String])] = {
    if (value == None) {
      return new ListBuffer[(String, ListBuffer[String])]
    }
    val intermediateList = value.asInstanceOf[JListWrapper[(String, JListWrapper[String])]].toList.to[ListBuffer]
    return intermediateList.map { case (str, lst) =>
      (str, lst.toList.to[ListBuffer])
    }.to[ListBuffer]
  }

  def apply(elasticMap: scala.collection.Map[String, AnyRef]): RegressionSettings = {
    val result = new RegressionSettings()

    //Go through the map from elasticsearch and populate the member fields
    result.wordRelevaceToggle = getBool(elasticMap("wordRelevaceToggle"))
    result.keywordsToggle = getBool(elasticMap("keywordsToggle"))
    result.distanceToggle = getBool(elasticMap("distanceToggle"))
    result.contanctInfoToggle = getBool(elasticMap("contanctInfoToggle"))
    result.resumeLengthToggle = getBool(elasticMap("resumeLengthToggle"))
    result.experienceToggle = getBool(elasticMap("experienceToggle"))

    result.keywordLists = getPairList(elasticMap("keywordLists"))
    result.positionKeywords = getList(elasticMap("positionKeywords"))
    result.degreeKeywords = getList(elasticMap("degreeKeywords"))

    return result
  }

  /**
   * This is the default apply method to turn on every feature, use tech keywords,
   *  and use Reston VA as the job location.
   */
  def apply(): RegressionSettings = {
    //In Progress
    return new RegressionSettings()
  }

}

class RegressionSettings() {
  //Toggles to turn features on or off
  var wordRelevaceToggle, keywordsToggle, distanceToggle, contanctInfoToggle, resumeLengthToggle, experienceToggle: Boolean = false

  //The location that you wish to measure distance from. Should be formatted similar to "Reston, VA"
  var jobLocation: String = ""

  //The set of keywords to look for
  var keywordLists = ListBuffer[(String, ListBuffer[String])]()

  //A list of words that relate to the required job opening
  var positionKeywords = ListBuffer[String]()

  //A list of words that relate to the degrees that are relevant
  var degreeKeywords = ListBuffer[String]()

  def toMap(): Map[String, Any] = {
    return Map (
      "wordRelevanceToggle" -> wordRelevaceToggle,
      "keywordsToggle" -> keywordsToggle,
      "distanceToggle" -> distanceToggle,
      "contanctInfoToggle" -> contanctInfoToggle,
      "resumeLengthToggle" -> resumeLengthToggle,
      "experienceToggle" -> experienceToggle,
      "keywordLists" -> keywordLists,
      "positionKeywords" -> positionKeywords,
      "degreeKeywords" -> degreeKeywords
    )
  }
}
