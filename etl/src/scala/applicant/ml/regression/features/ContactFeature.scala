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
    var sum = 0
    val contactTypes = values.asInstanceOf[ListBuffer[String]]
    for (contactType <- contactTypes) {
      contactType match {
        case ("linkedin") => sum += stringCounter(applicant.linkedin)
        case ("github") => sum += stringCounter(applicant.github)
        case ("indeed") => sum += stringCounter(applicant.indeed)
        case ("urls") => sum += applicant.urlList.length
        case ("email") => sum += stringCounter(applicant.email)
        case ("phone") => sum += stringCounter(applicant.phone)
      }
    }

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
