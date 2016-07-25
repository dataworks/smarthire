package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.ml.regression.FeatureSetting
import scala.collection.mutable.ListBuffer

class LengthFeature(newSetting: FeatureSetting) extends BaseFeature {
  val setting = newSetting

  /**
   * Measures resume length (note: may want to precompile regex if slow)
   *
   * @param applicant The ApplicantData object containing the resume
   * @return Resume length without punctuation, spaces, or newline characters
   */
  def getFeatureScore(applicant: ApplicantData): Double = {
    val resumeLength = applicant.fullText.replaceAll("[^a-zA-Z0-9]+","").length()
    val maxlength = setting.values.asInstanceOf[ListBuffer[Long]](0).toInt
    if (resumeLength >= maxlength) {
      return 1.0
    }
    else {
      return resumeLength.toDouble / maxlength
    }
  }
}
