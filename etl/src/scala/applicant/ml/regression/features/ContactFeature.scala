package applicant.ml.regression.features

import applicant.etl.ApplicantData
import scala.collection.mutable.ListBuffer

class ContactFeature(newName: String) extends BaseFeature {
  val name = newName

  /**
   * Counts number of contact information items
   *
   * @param applicant The applicant being ranked
   * @param values unused, empty array
   * @return The number of contact items found
   */
  def getFeatureScore(applicant: ApplicantData, values: ListBuffer[AnyRef]): Double = {
    val sum = stringCounter(applicant.linkedin) + stringCounter(applicant.github) + stringCounter(applicant.indeed) + applicant.urlList.length + stringCounter(applicant.email) + stringCounter(applicant.phone)
    if (sum >= 5.0) {
      return 1.0
    }
    else {
      return sum.toDouble/5.0
    }
  }

  /**
   * Chechs if a string is empty or not
   *
   * @param str String to check if empty or null
   * @return 0 if str is null or empty, 1 otherwise
   */
  def stringCounter(str: String) : Int = {
    if (str != null && !str.isEmpty()) {
      return 1
    }
    else {
      return 0
    }
  }
}
