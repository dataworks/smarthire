package applicant.ml.regression.features

import applicant.etl.ApplicantData
import applicant.ml.regression.FeatureSetting
import scala.collection.mutable.{ListBuffer, Map}
import scala.collection.JavaConversions.JListWrapper

class ExperienceFeature(newSetting: FeatureSetting) extends BaseFeature {
  val setting = newSetting

  /**
   * Will look through a list of keywords and check if the search string contains
   *  any of them
   */
  private def stringListContainment(search: String, lst: Seq[String]): Boolean = {
    val check = search.toLowerCase
    for (item <- lst) {
      if (check.contains(item)) {
        return true
      }
    }
    return false
  }

  /**
   * Will check if a position string contains certain keywords
   *  that indicate if it is a tech position
   */
  def checkPosition(currentScore: Double, pos: String, lst: Seq[String]): Double = {
    if (stringListContainment(pos, lst)) {
      return currentScore + 1.0
    }
    return currentScore
  }

  /**
   * Will check if a degree string contains certain keywords
   *  that indicate if it is a tech degree
   */
  def checkDegree(deg: String, lst: Seq[String]): Boolean = {
    return stringListContainment(deg, lst)
  }

  /**
   * Will scale a GPA by a specified amount and adjust
   *  it extra so that very low gpas do not give much credit
   */
  private def gpaScaler(gpa: Double, scale: Double): Double = {
    return (gpa*gpa*scale)/4.0
  }

  /**
   * Will take gpa, education, and past titles all into account
   *
   * Since gpa is often not included once experience is gained,
   *  a score for gpa is calculated, and a score for experience is
   *    calculated, and the higher score is the one that is chosen.
   *
   * Because both education and gpa are both related to school,
   *  the type of degree will scale the gpa
   *
   * @param applicant The applicant that is to be judged
   * @return A double corresponding to the level of historical aptitutde
   */
  def getFeatureScore(applicant: ApplicantData): Double = {

    val infoMap = setting.values(0).asInstanceOf[Map[String,JListWrapper[AnyRef]]]
    println("position/degree map: " + infoMap)
    val positionKeywords: ListBuffer[String] = infoMap("positions").asInstanceOf[JListWrapper[String]].toList.to[ListBuffer]
    val degreeKeywords: ListBuffer[String] = infoMap("degrees").asInstanceOf[JListWrapper[String]].toList.to[ListBuffer]
    var rawGPA = applicant.gpa

    //Scale the gpa by the type of degree
    if (checkDegree(applicant.degree, degreeKeywords)) {
      rawGPA = gpaScaler(rawGPA, 0.5)
    } else {
      rawGPA *= gpaScaler(rawGPA, 0.25)
    }

    var positionScore = 0.0
    positionScore = checkPosition(positionScore, applicant.recentTitle, positionKeywords)
    for (position <- applicant.otherTitleList) {
      positionScore = checkPosition(positionScore, applicant.recentTitle, positionKeywords)
    }

    val maxScore = Math.max(rawGPA, positionScore) / 4.0

    return if (maxScore > 1.0) 1.0 else maxScore
  }
}
