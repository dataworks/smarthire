package applicant.ml.regression.features

import applicant.etl.ApplicantData
import scala.collection.mutable.ListBuffer

class LengthFeature(newName: String) extends BaseFeature {
  val name = newName

  /**
   * Measures resume length (note: may want to precompile regex if slow)
   *
   * @param applicant The ApplicantData object containing the resume
   * @param values unused, empty array for now
   * @return Resume length without punctuation, spaces, or newline characters
   */
  def getFeatureScore(applicant: ApplicantData, values: ListBuffer[AnyRef]): Double = {
    val resumeLength = applicant.fullText.replaceAll("[^a-zA-Z0-9]+","").length()
    val maxlength = values.asInstanceOf[ListBuffer[Int]](0)
    if (resumeLength >= maxlength) {
      return 1.0
    }
    else {
      return resumeLength.toDouble / maxlength
    }
  }
}
