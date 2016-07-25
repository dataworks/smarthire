package applicant.ml.regression

import applicant.etl.EsUtils

import scala.collection.mutable.{ListBuffer, Map}
import scala.collection.JavaConversions._
import scala.collection.breakOut

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark._

object RegressionSettings {

  /**
   * Constructor for the map that is returned from the mlsettings index in elasticsearch.
   * The map is parsed into the RegressionSettings object.
   */
  def apply(elasticMap: scala.collection.Map[String, AnyRef]): RegressionSettings = {
    val result = new RegressionSettings()

    val intermediateMap: Map[String, Map[String, Map[String, AnyRef]]] = elasticMap.asInstanceOf[Map[String, Map[String, Map[String, AnyRef]]]]

    //for each general feature type
    for (featureTypeMap <- intermediateMap) {

      //Create a map that will store the feature instances with each of their settings
      val currentFeatureMap: Map[String, FeatureSetting] = Map()

      //for each instance of the feature type
      for (featureInstanceMap <- featureTypeMap._2) {

        //Get the name, enabled field, and values
        val currentFeatureName = EsUtils.checkSomeString(featureTypeMap._2.get("name"))
        val currentFeatureToggle = EsUtils.checkSomeBool(featureTypeMap._2.get("enabled"))
        val currentFeatureValues = EsUtils.checkSomeList(featureTypeMap._2.get("values"))

        currentFeatureMap += (featureInstanceMap._1 -> new FeatureSetting(currentFeatureName, currentFeatureToggle, currentFeatureValues))
      }

      //Add the current feature map to the featureSettingsMap
      result.featureSettingMap += (featureTypeMap._1 -> currentFeatureMap)
    }

    return result
  }

  /**
   * Constructor that automatically queries elasticsearch for the settings and
   *  creates a RegressionSettings out of the results.
   */
  def apply(sc: SparkContext): RegressionSettings = {
    val settingsMap = sc.esRDD("settings/setting").first()
    return apply(settingsMap._2)
  }

  /**
   * This is the default apply method to turn on every feature, use tech keywords,
   *  and use Reston VA as the job location.
   */
  def apply(): RegressionSettings = {
    val result = new RegressionSettings()
    //Create a Feature Setting for all of the features that need to be included
    val relevance = new FeatureSetting("Relevance", true, new ListBuffer[AnyRef])
    val bigData = new FeatureSetting("BigData", true, ListBuffer("Spark","Hadoop","HBase","Hive","Cassandra","MongoDB","Elasticsearch","Docker","AWS","HDFS","MapReduce","Yarn","Solr","Avro","Lucene","Kibana", "Kafka"))
    val databases = new FeatureSetting("Databases", true, ListBuffer("Oracle","Postgresql","Mysql","SQL"))
    val etl = new FeatureSetting("ETL", true, ListBuffer("Pentaho","Informatica","Streamsets","Syncsort"))
    val webApp = new FeatureSetting("WebApp", true, ListBuffer("AngularJS","Javascript","Grails","Spring","Hibernate","node.js","CSS","HTML"))
    val mobile = new FeatureSetting("Mobile", true, ListBuffer("Android","iOS","Ionic","Cordova","Phonegap"))
    val languages = new FeatureSetting("Languages", true, ListBuffer("Java","Scala","Groovy","C","Python","Ruby","Haskell"))
    val proximity = new FeatureSetting("Proximity", true, ListBuffer("Reston, VA"))
    val contact = new FeatureSetting("ContactInfo", true, ListBuffer("linkedin", "github", "indeed", "urls", "email", "phone"))
    val length = new FeatureSetting("Resume Length", true, new ListBuffer[AnyRef])
    val experience = new FeatureSetting("Experience", true, ListBuffer(Map("positions" -> ListBuffer("technology", "computer", "information", "engineer", "developer", "software", "analyst", "application", "admin"), "degrees" -> ListBuffer("tech", "computer", "information", "engineer", "c.s.", "programming", "I.S.A.T."))))

    val fullMap: Map[String, Map[String, FeatureSetting]] = Map()

    val relevanceMap = Map("relevance" -> relevance)
    val keywordsMap = Map("bigData" -> bigData, "dbms" -> databases, "etl" -> etl, "webApp" -> webApp, "mobile" -> mobile, "languages" -> languages)
    val proximityMap = Map("reston" -> proximity)
    val contactMap = Map("allInfo" -> contact)
    val lengthMap = Map("standardLength" -> length)
    val experienceMap = Map("techExperience" -> experience)

    result.featureSettingMap += ("relevance" -> relevanceMap)
    result.featureSettingMap += ("keywords" -> keywordsMap)
    result.featureSettingMap += ("jobLocation" -> proximityMap)
    result.featureSettingMap += ("contactInfo" -> contactMap)
    result.featureSettingMap += ("resumeLength" -> lengthMap)
    result.featureSettingMap += ("experience" -> experienceMap)

    return result
  }

}

class RegressionSettings() extends Serializable {
  val featureSettingMap: Map[String, Map[String, FeatureSetting]] = Map()

  /**
   * Returns a map of the internal data.
   * This map can be directly uploaded into the mlsettings elasticsearch index
   */
  def toMap(): Map[String, Map[String, Map[String, Any]]] = {
    return featureSettingMap.map { case (category, categoryMap) =>
      (category, categoryMap.map{ case (instance, setting) =>
        (instance, setting.toMap())
      })
    }
  }
}

class FeatureSetting(featureName: String, isEnabled: Boolean, featureValues: ListBuffer[AnyRef]) {
  val name: String = featureName
  val enabled: Boolean = isEnabled
  val values = featureValues

  def toMap(): Map[String, Any] = {
    return Map("name" -> name, "enabled" -> enabled, "values" -> values)
  }
}
