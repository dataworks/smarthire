package applicant.ml.regression

import applicant.nlp.LuceneTokenizer
import applicant.etl._
import applicant.ml.naivebayes._
import applicant.ml.regression.features._

import scala.util.Try
import scala.collection.mutable.{ListBuffer, Map, HashMap}
import scala.collection.breakOut
import java.util.regex
import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel, IDFModel}
import org.apache.spark.mllib.linalg.{Vectors, Vector}
import org.apache.spark.mllib.classification.{NaiveBayesModel, NaiveBayes}

import org.slf4j.{Logger, LoggerFactory}

/**
 * Logistic Feature Generator is a class that will calculate feature scores from an applicant.
 * It also has functions to zip feaure names
 */
object LogisticFeatureGenerator {
  def apply(bayesModel: NaiveBayesModel, idfModel: IDFModel, settings: RegressionSettings, cityFileLoc: String) : LogisticFeatureGenerator = {
    new LogisticFeatureGenerator(bayesModel, idfModel, settings, cityFileLoc)
  }

  /**
   *  Will return a list of the names of all the features that
   *    were given in settings and are toggled on
   *
   *  @param settings The settings whose features are to be extracted
   *  @return The list of feature names
   */
  def getFeatureList(settings : RegressionSettings) : List[String] = {
    val featureList : ListBuffer[String] = new ListBuffer()

    return featureList.toList
  }

  /**
   * Will return a map of feature names with zeros for scores
   *
   * @param settings The settings whose features will be mapped to 0
   * @return a map of features to 0.0
   */
  def getEmptyFeatureMap(settings: RegressionSettings): Map[String, Double] = {
    return this.getFeatureList(settings).map ( feature => (feature, 0.0) )(breakOut): Map[String,Double]
  }

  /**
   * Will return a map of feature names with the provided values
   *
   * @param vec A vector of feature scores that are to be associated
   *              with their feature names
   * @return A map of feature names with their score
   */
  def getPopulatedFeatureMap(vec: Vector, settings: RegressionSettings): Map[String, Double] = {
    val featureVals = vec.toArray
    return (this.getFeatureList(settings) zip featureVals)(breakOut): Map[String,Double]
  }

}

/**
 * FeatureGenerator
 */
class LogisticFeatureGenerator(bayesModel: NaiveBayesModel, idfModel: IDFModel, settings: RegressionSettings, cityFileLoc: String) extends Serializable {

  val featuresList: ListBuffer[BaseFeature] = ListBuffer()

  {
    //Get the location map data so that the Location Features do not all recreate it
    val locationMap: HashMap[(String, String), (Double, Double)] = {

      val result = HashMap[(String, String), (Double, Double)]()
      val lines = scala.io.Source.fromFile(cityFileLoc).getLines()

      for (line <- lines) {
        val splitVals = line.toLowerCase().split("#")//#split
        result += ((splitVals(0), splitVals(1)) -> (splitVals(2).toDouble, splitVals(3).toDouble))
      }
      result
    }

    //For each FeatureType in settings (jobLocation, experience, etc.)
    for (featureType <- settings.featureSettingMap) {

      //For each feature in the feature type
      for (featureInstanceTuple <- featureType._2) {
        val featureInstance = featureInstanceTuple._2

        //if the feature instance is turned on
        if (featureInstance.enabled) {

          //Add it to the list of features
          featureType._1 match {
            case "jobLocation" =>
              featuresList += new ProximityFeature(featureInstance, locationMap)
            case "experience" =>
              featuresList += new ExperienceFeature(featureInstance)
            case "resumeLength" =>
              featuresList += new LengthFeature(featureInstance)
            case "contactInfo" =>
              featuresList += new ContactFeature(featureInstance)
            case "relevance" =>
              featuresList += new RelevanceFeature(featureInstance, bayesModel, idfModel)
            case "keywords" =>
              featuresList += new KeywordFeature(featureInstance)
            case _ =>
          }
        }
      }
    }
  }

  /**
   * Will return a vector of the features that were specified in the settings
   *
   * @param model A word2VecModel uses to find synonyms
   * @param applicant The applicant whose features are needed
   * @return A vector that corresponds to the feature scores
   */
  def getLogisticFeatureVec(applicant: ApplicantData): Vector = {
    val featureArray = scala.collection.mutable.ArrayBuffer.empty[Double]

    for (feature <- featuresList) {
      featureArray += feature.getFeatureScore(applicant)
    }

    return Vectors.dense(featureArray.toArray[Double])
  }

}
