package applicant.etl

import applicant.nlp._

import scala.collection.mutable.{ListBuffer, Map, LinkedHashSet}

/**
 *
 *@version 0.0.1
 *
 */

object EntityMapper {
  /**
   * Pulls in the results from extractText and EntityGrabber and ouputs as formatted map for ES
   *
   * @param taggedEntities A LinkedHashSet object from the EntityGrabber class
   * @param applicantID A String to be used as the applicant's unique ID
   * @param fullText A String of the full parsed resume from extractText
   * @return A map formatted to save to ES as JSON
   */
  def createMap(taggedEntities: LinkedHashSet[(String, String)], applicantID: String, fullText: String): Map[String, Object] = {
    val notFound: String = ""

    var name, recentTitle, recentLocation, recentOrganization, degree, school, gpa, url, email, phone: String = notFound
    var score: Double = 0.0
    val skillsList: ListBuffer[Map[String,String]] = new ListBuffer[Map[String,String]]()
    val otherTitleList: ListBuffer[String] = new ListBuffer[String]()
    val otherLocationList: ListBuffer[String] = new ListBuffer[String]()
    val otherOrganizationList: ListBuffer[String] = new ListBuffer[String]()

    //degree, location, organization, person, school, title, bigdata, database, etl, webapp, mobile, language, gpa, email, phone, url

    taggedEntities.foreach { pair =>
      pair match {
        case ("degree", _) if (degree == notFound) => degree = pair._2
        case ("location", _) => if (recentLocation == notFound) { recentLocation = pair._2 }
          otherLocationList += pair._2
        case ("organization", _)  => if (recentOrganization == notFound) { recentOrganization = pair._2 }
          otherOrganizationList += pair._2
        case ("person", _) if (name == notFound) => name = pair._2
        case ("school", _) if (school == notFound) => school = pair._2
        case ("title", _) => if (recentTitle == notFound) { recentTitle = pair._2 }
          otherTitleList += pair._2
        case ("bigdata", _) | ("database", _) | ("etl", _) | ("webapp", _) | ("mobile", _) | ("language", _)  => skillsList += Map(pair._1 -> pair._2.toLowerCase())
        case ("gpa", _) if (gpa == notFound) => gpa = pair._2
        case ("email", _) if (email == notFound) => email = pair._2
        case ("phone", _) if (phone == notFound) => phone = pair._2
        case _ =>
      }
    }

    val strScore: String = String.valueOf(score)

    if (url.equalsIgnoreCase(notFound)) {
      url = ""
    }
    if (gpa.equalsIgnoreCase(notFound)) {
      gpa = ""
    }

    val map: Map[String, Object] = Map(
      "id" -> applicantID,
      "name" -> name,
      "score" -> strScore,
      "currentLocation" -> Map(
        "title" -> recentTitle,
        "location" -> recentLocation,
        "organization" -> recentOrganization
      ),
      "skills" -> skillsList,
      "education" -> Map(
        "degree" -> degree,
        "school" -> school,
        "gpa" -> gpa
      ),
      "contact" -> Map(
        "url" -> url,
        "email" -> email,
        "phone" -> phone
      ),
      "additionalInfo" -> Map(
        "pastPostions" -> Map(
          "title" -> otherTitleList,
          "location" -> otherLocationList,
          "organization" -> otherOrganizationList
        ),
        "resume" -> fullText
      )
    )

    return map
  }
}
