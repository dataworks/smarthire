package applicant.ml.regression.features

import applicant.etl.ApplicantData
import scala.collection.mutable.ListBuffer

abstract class BaseFeature {
  //The name of the feature
  val name: String

  /**
   *  Will return a score for the type of feature
   *
   * @param applicant The applicant whose feature is checked
   * @param values Any configurable options for the feature
   */
  def getFeatureScore(applicant: ApplicantData, values: ListBuffer[AnyRef]): Double
}
