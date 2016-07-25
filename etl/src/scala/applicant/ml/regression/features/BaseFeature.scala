package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.ml.regression.FeatureSetting
import scala.collection.mutable.ListBuffer

abstract class BaseFeature extends Serializable {
  //A setting to be used when getting the feature score
  val setting: FeatureSetting

  def name: String = {
    setting.name
  }

  /**
   *  Will return a score for the type of feature
   *
   * @param applicant The applicant whose feature is checked
   * @param values Any configurable options for the feature
   */
  def getFeatureScore(applicant: ApplicantData): Double
}
