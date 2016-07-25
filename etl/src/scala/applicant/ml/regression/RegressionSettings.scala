package applicant.ml.regression

import applicant.etl.EsUtils

import scala.collection.mutable.{ListBuffer, Map}
import scala.collection.JavaConversions._

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

    return result
  }

}

class RegressionSettings() extends Serializable {
  val featureSettingMap: Map[String, Map[String, FeatureSetting]] = Map()

  /**
   * Returns a map of the internal data.
   * This map can be directly uploaded into the mlsettings elasticsearch index
   */
  def toMap(): Map[String, Any] = {
    return Map()
  }
}

class FeatureSetting(featureName: String, isEnabled: Boolean, featureValues: ListBuffer[AnyRef]) {
  val name: String = featureName
  val enabled: Boolean = isEnabled
  val values = featureValues
}
