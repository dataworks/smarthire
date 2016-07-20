package applicant.ml.regression

import scala.collection.mutable.Map
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
  private def getList(value: AnyRef): List[String] = {
    if (value == None) {
      return List()
    }
    return value.asInstanceOf[JListWrapper[String]].toList
  }

  /**
   * Will check if a list of pair of string and list is None and convert it accordingly
   */
  private def getPairList(value: AnyRef): List[(String, List[String])] = {
    if (value == None) {
      return List()
    }
    val intermediateList = value.asInstanceOf[JListWrapper[(String, JListWrapper[String])]].toList
    return intermediateList.map { case (str, lst) =>
      (str, lst.toList)
    }
  }

  def apply(elasticMap: scala.collection.Map[String, AnyRef]): RegressionSettings = {
    val result = new RegressionSettings()

    //Go through the map from elasticsearch and populate the member fields
    result.wordRelevaceToggle = getBool(elasticMap("wordRelevaceToggle"))
    result.keywordsToggle = getBool(elasticMap("keywordsToggle"))
    result.distanceToggle = getBool(elasticMap("distanceToggle"))
    result.contactInfoToggle = getBool(elasticMap("contactInfoToggle"))
    result.resumeLengthToggle = getBool(elasticMap("resumeLengthToggle"))
    result.experienceToggle = getBool(elasticMap("experienceToggle"))

    result.keywordLists = getPairList(elasticMap("keywordLists"))
    result.positionKeywords = getList(elasticMap("positionKeywords"))
    result.degreeKeywords = getList(elasticMap("degreeKeywords"))

    return result
  }

  def apply(sc: SparkContext): RegressionSettings = {
    val settingsMap = sc.esRDD("mlsettings/settings").collect()(0)
    return apply(settingsMap._2)
  }

  /**
   * This is the default apply method to turn on every feature, use tech keywords,
   *  and use Reston VA as the job location.
   */
  def apply(): RegressionSettings = {
    val result = new RegressionSettings()
    result.wordRelevaceToggle = true
    result.keywordsToggle = true
    result.distanceToggle = true
    result.contactInfoToggle = true
    result.resumeLengthToggle = true
    result.experienceToggle = true

    result.keywordLists = List(("Big Data", List("Spark","Hadoop","HBase","Hive","Cassandra","MongoDB","Elasticsearch","Docker","AWS","HDFS","MapReduce","Yarn","Solr","Avro","Lucene","Kibana", "Kafka")),
    ("DatabaseEngineering", List("Oracle","Postgresql","Mysql","SQL")),
    ("ETL Engineering", List("Pentaho","Informatica","Streamsets","Syncsort")),
    ("Web App Development", List("AngularJS","Javascript","Grails","Spring","Hibernate","node.js","CSS","HTML")),
    ("Mobile Development", List("Android","iOS","Ionic","Cordova","Phonegap")),
    ("Common Programming Languages", List("Java","Scala","Groovy","C","Python","Ruby","Haskell")))

    result.positionKeywords = List("technology", "computer", "information", "engineer", "developer", "software", "analyst", "application", "admin")

    result.degreeKeywords = List("tech", "computer", "information", "engineer", "c.s.", "programming", "I.S.A.T.")

    return result
  }

}

class RegressionSettings() {
  //Toggles to turn features on or off
  var wordRelevaceToggle, keywordsToggle, distanceToggle, contactInfoToggle, resumeLengthToggle, experienceToggle: Boolean = false

  //The location that you wish to measure distance from. Should be formatted similar to "Reston, VA"
  var jobLocation: String = ""

  //The set of keywords to look for
  var keywordLists = List[(String, List[String])]()

  //A list of words that relate to the required job opening
  var positionKeywords = List[String]()

  //A list of words that relate to the degrees that are relevant
  var degreeKeywords = List[String]()

  def toMap(): Map[String, Any] = {
    return Map (
      "id" -> "current",
      "wordRelevanceToggle" -> wordRelevaceToggle,
      "keywordsToggle" -> keywordsToggle,
      "distanceToggle" -> distanceToggle,
      "contactInfoToggle" -> contactInfoToggle,
      "resumeLengthToggle" -> resumeLengthToggle,
      "experienceToggle" -> experienceToggle,
      "keywordLists" -> keywordLists,
      "positionKeywords" -> positionKeywords,
      "degreeKeywords" -> degreeKeywords
    )
  }
}
